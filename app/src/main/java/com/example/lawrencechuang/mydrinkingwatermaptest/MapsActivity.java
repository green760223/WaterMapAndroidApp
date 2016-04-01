package com.example.lawrencechuang.mydrinkingwatermaptest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener, GoogleMap.OnCameraChangeListener {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private UiSettings uiSettings;
    private LocationManager locationManager;
    private LocationManager mlocationMgr;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    int[] menuIcons = {R.drawable.about_us, R.drawable.contact_us, R.drawable.infomation, R.drawable.questions,
            R.drawable.login, R.drawable.my_water_point};

    //    int[] menuIcons = {R.drawable.animals_dog_icon, R.drawable.animals_dog_icon, R.drawable.animals_dog_icon,
//            R.drawable.animals_dog_icon, R.drawable.animals_dog_icon, R.drawable.animals_dog_icon};
    List<Integer> icons = new ArrayList<Integer>();
    private String[] drawer_menu;
    List<WaterPointsModel> waterPointsModelList = new ArrayList<WaterPointsModel>();
    String provider;
    Location coor;
    private boolean isGpsEnable = false;
    private boolean isNetworkEnable = false;
    Double lat;
    Double lng;
    Double posLat;
    Double posLng;
    float posZoom;
    private String posResult;
    private DBHelper helper;
    private Cursor cursor;
    private Double lastLat;
    private Double lastLng;
    private Float lastZoom;
    private Cursor lastPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /**
         * 初始化sqlite資料庫並建立資料表儲存上次離開app的經緯度位置與地圖縮放大小
         */
        helper = new DBHelper(this, "location.db", null, 1);

        /**
         * 查詢使用者上次離開app前的座標
         */
        queryLastPositionFromSQLite();

        /*檢查是否具有建立資料表*/
//        Boolean isTable = isTableExist();
//        Log.d("=boolean=", isTable + "");


        //建立toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Water Map");
        toolbar.setLogo(R.mipmap.waterdrop);
        setSupportActionBar(toolbar);  //setNavigationIcon要在setSupportActionBar之後才會生效
        toolbar.setNavigationIcon(R.drawable.ic_menu_white);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String msg = "";

                //處理Menu選項
                switch (item.getItemId()) {
                    case R.id.search:
                        msg += "點選搜尋水點";
                        break;
                }

                if (!msg.equals("")) {
                    Toast.makeText(MapsActivity.this, msg, Toast.LENGTH_SHORT).show();
                }

                return true;
            }

        });

        //open up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerToggle.syncState();

        drawerLayout.setDrawerListener(drawerToggle);

        //讀取navigation的欄位名稱
        String[] drawMenu = this.getResources().getStringArray(R.array.left_draw);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawMenu);

        List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

        for (int i = 0; i < menuIcons.length; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("icon", Integer.toString(menuIcons[i]));
            map.put("title", drawMenu[i]);
            data.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.drawer_list_item2, new String[]{"icon", "title"},
                new int[]{R.id.imgIcon});

        ListView firstDrawer = (ListView) findViewById(R.id.drawer1);
        firstDrawer.setAdapter(adapter);

        //String s = "http://192.168.1.103:8080/WaterMap/api/v1/waterPoints/getAllWaterPoints";

        mlocationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
//        getProvider();
        Log.d("PROVIDER", getProvider());

        //取得所有水點基本資訊API位址
        String s = "http://drinkingwatermap-watermap.rhcloud.com/WaterMap/api/v1/waterPoints/getAllBasicWaterPoints";
        new getAllWaterPoint().execute(s);

    }


    @Override
    protected void onResume()
    {

        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        isGpsEnable = mlocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = mlocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Log.d("=xisGpsEnablex=", isGpsEnable + "");
        Log.d("=xisNetWorkEnablex=", isNetworkEnable + "");

        /**
         * gps and network同時關閉
         */
//        if(!isGpsEnable && !isNetworkEnable) {
//
//            Log.d("phase1", "phase1");
//            Log.d("mlocation", mlocationMgr + "");

//            mlocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, this);
//            coor = mlocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//            Log.d("==gggg==", coor + "");
//
//            mlocationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, this);
//            Location test = mlocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            Log.d("==nnnn==", test + "");
//
//            Log.d("=lastknowgps=", mlocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER).toString());
//            Log.d("=lastknownet=", mlocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).toString());
//
//
//            if (mlocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
//
//                coor = mlocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                lat = coor.getLatitude();
//                lng = coor.getLongitude();
//
//                Log.d("=noGPS=", lat + "/" + lng);
//
//            }
//
//            if (mlocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
//
//                coor = mlocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                lat = coor.getLatitude();
//                lng = coor.getLongitude();
//
//                Log.d("=noNETGPS=", lat + "/" + lng);
//
//            }
//
//        } else {
//
//            Log.d("=phase2=", "phase2");
//
//            /**
//             * if network is enable
//             */
//            if(isNetworkEnable) {
//
//                mlocationMgr.requestLocationUpdates(
//                        LocationManager.NETWORK_PROVIDER,
//                        3000,
//                        0,
//                        this);
//
//                if(mlocationMgr != null) {
//                    coor = mlocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//
//                    lat = coor.getLatitude();
//                    lng = coor.getLongitude();
//                    Log.d("=LastKnownNetLocation=", lat + "/" + lng);
//                }
//                Log.d("=isNetworkEnable=", coor + "");
//            }
//
//            /**
//             * if gps is enable
//             */
//            if(isGpsEnable) {
//
//                if(coor == null) {
//                    mlocationMgr.requestLocationUpdates(
//                            LocationManager.GPS_PROVIDER,
//                            3000,
//                            0,
//                            this);
//
//                    if(mlocationMgr != null) {
//                        coor = mlocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//                        if(coor != null) {
//                            lat = coor.getLatitude();
//                            lng = coor.getLongitude();
//
//                            Log.d("=LastKnownGpsLocation=", lat + "/" + lng);
//                        }
//                    }
//                    Log.d("=isGpsEnable=", coor + "");
//                }
//            }
//        }


       //mlocationMgr.requestLocationUpdates(provider, 3000, 0, this);
//        Log.d("resBest", provider + "");

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("=start=", "start");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("=pause=", "pause");
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d("=stop=", "stop" + "/" + posResult);

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("=destroy=", "destroy" + "/" + posResult);
        insertPosition(posResult);
        helper.close();
        Log.d("=DB=", "CLOSE");
    }


    public String getProvider()
    {
        if(mlocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            provider = LocationManager.GPS_PROVIDER;
        }
        else
        {
            provider = LocationManager.NETWORK_PROVIDER;
        }

        return provider;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnCameraChangeListener(this);


        //取消導航與切換到網路版google map
        mMap.getUiSettings().setMapToolbarEnabled(false);

//        if(coor != null)
//        {
//            LatLng point = new LatLng(coor.getLatitude(), coor.getLongitude());
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
//        }

        /**回到使用者最後離開app的畫面座標, 若第一次安裝app則預設定位到臺北101*/
        if(lastPoint.getCount() != 0) {
            /*move camera to the location that user last time visited.*/
            LatLng point = new LatLng(lastLat, lastLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, lastZoom));
        } else {
            /*move camera to Taipei 101.*/
            LatLng point = new LatLng(25.0334784, 121.5618758);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

//        Location location = mlocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        Log.d("locationGPS", location.getLatitude() + "/" + location.getLongitude());
//
//        Location location1 = mlocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        Log.d("locationNET", location1.getLatitude() + "/" + location1.getLongitude());

//        boolean isGps = mlocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        boolean isNetwork = mlocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

//        if(isGps){
//
//
//
//        } else if (isNetwork){
//
//
//
//        } else{
//
//
//
//        }
//
//        Log.d("==isGps==", isGps + "");
//
//        Log.d("==isNetwork==", isNetwork + "");


//        isGpsOpen();


        //To get users's location
        mMap.setOnMyLocationButtonClickListener((GoogleMap.OnMyLocationButtonClickListener) this);
        enableMyLocation();

//        Log.d("gps", isGpsOpen() + "");

        //boolean isOnMyLocationButtonClick = onMyLocationButtonClick();

//        if(isGpsOpen())
//        {
//            //To get users's location
//            mMap.setOnMyLocationButtonClickListener((GoogleMap.OnMyLocationButtonClickListener) this);
//            enableMyLocation();
//        }
//        else
//        {
//            openGps();
//        }

    }


    public void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {

        //偵測使用者是否開啟GPS，若無則導引至開啟GPS設定頁面
        openGps();

        if (isGpsOpen()) {
            Toast.makeText(this, "位置定位中", Toast.LENGTH_SHORT).show();
        }

        return false;
    }


    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        } else {
            mPermissionDenied = true;
        }
    }


    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }


    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "請開啟GPS定位系統");
    }


    /**
     * 判斷GPS是否開啟，GPS或者AGPS開啟一個就認為是開啟的
     */
    private boolean isGpsOpen() {
        mlocationMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //通過GPS衛星定位，定位級別可以精確到街（通過24顆衛星定位，在室外和空曠的地方定位準確、速度快）
        boolean gps = mlocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);

        //通過WLAN或移動網路(3G/2G)確定的位置（也稱作AGPS，輔助GPS定位。主要用於在室內或遮蓋物（建築群或茂密的深林等）密集的地方定位）
        boolean network = mlocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);



        //只要其中一個開啟就回傳true
        if (gps == true || network == true) {
            Log.d("==both==", gps + "/" + network);
            return true;
        }

        return false;
    }


    /**
     * 詢問使用者是否要開啟gps
     */
    public void openGps() {
        if (!mlocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(MapsActivity.this)
                    .setTitle("地圖工具")
                    .setMessage("您尚未開啟定位服務，要前往設定頁面啟動定位服務嗎？")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }

                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MapsActivity.this, "未開啟定位服務，無法取得當前位置!", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        }
    }


    //切換至新增水點頁面
    public void addWaterPoint(View view) {
        //Toast.makeText(MapsActivity.this, "Floating Button is clicked", Toast.LENGTH_SHORT).show();

        //切換到新增水點頁面
        Intent addWaterPoint = new Intent();
        addWaterPoint.setClass(MapsActivity.this, AddWaterPointActivity.class);
        startActivity(addWaterPoint);
        //overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }


    @Override
    public void onCameraChange(CameraPosition position) {

        String test = saveLocationOnCamera(position);
        //Log.d("=testCamera=", test);


        //Log.d("=position=", posLat + "/" + posLng + "/" + posZoom);
//        Toast.makeText(MapsActivity.this, posLat + "/" + posLng + "/" + posZoom, Toast.LENGTH_SHORT).show();

    }


    public String saveLocationOnCamera(CameraPosition position) {

        posLat = position.target.latitude;
        posLng = position.target.longitude;
        posZoom = position.zoom;

        posResult = posLat + "/" + posLng + "/" + posZoom;

        Log.d("=ppp=", posResult);

        return posResult;
    }


    /**
     * 執行查詢所有水點API
     */
    class getAllWaterPoint extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuffer sb = new StringBuffer();

            try {
                URL url = new URL(params[0]);
                Log.d("url", url + "");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = in.readLine();

                while (line != null) {
                    sb.append(line);
                    line = in.readLine();
                    Log.d("sb", sb + "");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return sb.toString();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("JSON", s);
            try {
                //解析從api傳回來的json字串
                JSONArray results = new JSONArray(s);
                List<Marker> markers = new ArrayList<Marker>();

                for (int i = 0; i < results.length(); i++) {
                    WaterPointsModel waterPointsModel = new WaterPointsModel();
                    JSONObject waterPoint = results.getJSONObject(i);
                    JSONObject location = waterPoint.getJSONObject("location");

                    waterPointsModel.setLongitude(location.getDouble("longitude"));
                    waterPointsModel.setLatitude(location.getDouble("latitude"));
                    waterPointsModel.setWaterPointName(waterPoint.getString("waterPointName"));
                    waterPointsModel.setDescription(waterPoint.getString("description"));
                    waterPointsModelList.add(waterPointsModel);

                    double lat = location.getDouble("longitude");
                    double lng = location.getDouble("latitude");
                    String name = waterPoint.getString("waterPointName");
                    String description = waterPoint.getString("description");
                    Log.d("WaterPoint:", name + "/" + lat + "/" + lng);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                                    .title(name)
                                    .position(new LatLng(lat, lng))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.waterdrop))
                                    .snippet(description)
                    );
                    markers.add(marker);
//                    Log.d("name:", waterPointsModel.getWaterPointName() + "");
                }

                Log.d("markers:", markers.size() + "");


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    public void insertPosition(String position) {

        String[] positionResult = position.split("/");
        Double lat = Double.parseDouble(positionResult[0]);
        Double lng = Double.parseDouble(positionResult[1]);
        Double zoom = Double.parseDouble(positionResult[2]);

        Log.d("=idLat=", lat + "");
        Log.d("=idLng=", lng + "");
        Log.d("=idZoom=", zoom + "");

        ContentValues values = new ContentValues();
        values.put("latitude", lat);
        values.put("longitude", lng);
        values.put("zoom", zoom);
        long id = helper.getWritableDatabase().insert("location_table", null, values);

        Log.d("=id=", id + "");

    }


    /**
     * 判斷table是否存在
     */
    public boolean isTableExist() {

        boolean isTableExist = true;
        Cursor cursorTest = null;

        try {
            cursorTest = helper.getReadableDatabase().rawQuery("SELECT * FROM location_table", null);
            Log.d("=testCount=", cursorTest.getCount() + "");

            if(cursorTest.getCount() == 0 || cursorTest == null) {
                isTableExist = false;
            }

        } catch (Exception e) {
            Log.d("=ERROR=", "ERROR");
        }

        return isTableExist;
    }


    /**
     * 查詢使用者最近一次離開app時的地圖位置與地圖縮放大小
     */
    public Cursor queryLastPositionFromSQLite() {

        /*查詢最後一筆座標資料*/
        lastPoint = helper.getReadableDatabase().rawQuery(
                "SELECT * FROM location_table WHERE _id = (SELECT MAX(_id) FROM location_table)", null
        );

        /*將最後一筆座標資料讀出*/
        if(lastPoint.getCount() != 0) {
            lastPoint.moveToFirst();
            lastLat = lastPoint.getDouble(lastPoint.getColumnIndex("latitude"));
            lastLng = lastPoint.getDouble(lastPoint.getColumnIndex("longitude"));
            lastZoom = lastPoint.getFloat(lastPoint.getColumnIndex("zoom"));
            Integer id = lastPoint.getInt(lastPoint.getColumnIndex("_id"));
            Log.d("=cur=", lastPoint.getCount() + "");
            Log.d("=cur=", id + "/" + lastLat + "/" + lastLng + "/" + lastZoom);
        }

        return lastPoint;
    }


    @Override
    public void onLocationChanged(Location location) {
        coor = location;
        Log.d("coor", coor + "");

        Log.d("==location==", location + "");

//        mMap.animateCamera(CameraUpdateFactory.newLatLng(
//                new LatLng(location.getLatitude(), location.getLongitude())));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

        Log.d("enableStringProvide", provider + "");
        Toast.makeText(MapsActivity.this, provider + "定位功能開啟", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onProviderDisabled(String provider) {

        Log.d("diableStringProvide", provider + "");
        Toast.makeText(MapsActivity.this, provider + "定位功能關閉", Toast.LENGTH_LONG).show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //為了讓Toolbar的Menu有作用，這邊的程式不可以拿掉
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


}
