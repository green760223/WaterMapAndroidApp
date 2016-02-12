package com.example.lawrencechuang.mydrinkingwatermaptest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
                                                   ActivityCompat.OnRequestPermissionsResultCallback
{
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
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    int[] menuIcons = {R.drawable.about_us, R.drawable.contact_us, R.drawable.infomation, R.drawable.questions,
            R.drawable.login, R.drawable.my_water_point};

//    int[] menuIcons = {R.drawable.animals_dog_icon, R.drawable.animals_dog_icon, R.drawable.animals_dog_icon,
//            R.drawable.animals_dog_icon, R.drawable.animals_dog_icon, R.drawable.animals_dog_icon};
    List<Integer> icons = new ArrayList<Integer>();
    private String[] drawer_menu;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        for(int i=0; i< menuIcons.length; i++)
        {
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

        //取得所有水點基本資訊API位址
        String s = "http://drinkingwatermap-watermap.rhcloud.com/WaterMap/api/v1/waterPoints/getAllBasicWaterPoints";
        new getAllWaterPoint().execute(s);

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
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        //取消導航與切換到網路版google map
        mMap.getUiSettings().setMapToolbarEnabled(false);

        //Add a default position and marker at Taiwan Environmental Info Association(TEIA) and move the camera
        LatLng teia = new LatLng(25.0003160, 121.5384950);
        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.waterdrop))
                .position(teia)
                .title("台灣環境資訊協會"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(teia, 15));

        //To get users's location
        mMap.setOnMyLocationButtonClickListener((GoogleMap.OnMyLocationButtonClickListener) this);
        enableMyLocation();

        Log.d("gps", isGpsOpen() +"");

        if(isGpsOpen())
        {
            //To get users's location
            mMap.setOnMyLocationButtonClickListener((GoogleMap.OnMyLocationButtonClickListener) this);
            enableMyLocation();
        }
        else
        {
            openGps();
        }

    }


    public void enableMyLocation()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
        {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        else if (mMap != null)
        {
            mMap.setMyLocationEnabled(true);
        }
    }



    @Override
    public boolean onMyLocationButtonClick()
    {
        //偵測使用者是否開啟GPS，若無則導引至開啟GPS設定頁面
        openGps();

        if(isGpsOpen())
        {
            Toast.makeText(this, "位置定位中", Toast.LENGTH_SHORT).show();
        }

        return false;
    }


    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode != LOCATION_PERMISSION_REQUEST_CODE)
        {
            return;
        }

        if(PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION))
        {
            enableMyLocation();
        }
        else
        {
            mPermissionDenied = true;
        }
    }


    protected void onResumeFragments()
    {
        super.onResumeFragments();
        if(mPermissionDenied)
        {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }


    private void showMissingPermissionError()
    {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "請開啟GPS定位系統");
    }


    /**
     * 判斷GPS是否開啟，GPS或者AGPS開啟一個就認為是開啟的
     */
    private boolean isGpsOpen()
    {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //通過GPS衛星定位，定位級別可以精確到街（通過24顆衛星定位，在室外和空曠的地方定位準確、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        //通過WLAN或移動網路(3G/2G)確定的位置（也稱作AGPS，輔助GPS定位。主要用於在室內或遮蓋物（建築群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

        //只要其中一個開啟就回傳true
        if(gps == true || network == true)
        {
            return true;
        }

        return false;
    }


    /**
     * 詢問使用者是否要開啟gps
     */
    public void openGps()
    {
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            new AlertDialog.Builder(MapsActivity.this)
                    .setTitle("地圖工具")
                    .setMessage("您尚未開啟定位服務，要前往設定頁面啟動定位服務嗎？")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }

                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Toast.makeText(MapsActivity.this, "未開啟定位服務，無法使用本功能!", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        }
    }


    //切換至新增水點頁面
    public void addWaterPoint(View view)
    {
        //Toast.makeText(MapsActivity.this, "Floating Button is clicked", Toast.LENGTH_SHORT).show();

        //切換到新增水點頁面
        Intent addWaterPoint = new Intent();
        addWaterPoint.setClass(MapsActivity.this, AddWaterPointActivity.class);
        startActivity(addWaterPoint);
        //overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }


    /**
     * 執行查詢所有水點API
     */
    class getAllWaterPoint extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... params)
        {
            StringBuffer sb = new StringBuffer();

            try
            {
                URL url = new URL(params[0]);
                Log.d("url", url+"");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = in.readLine();

                while (line != null)
                {
                    sb.append(line);
                    line = in.readLine();
                    Log.d("sb", sb+"");
                }
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return sb.toString();
        }

        @Override
        protected void onProgressUpdate(Void... values)
        {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String s)
        {
           Log.d("JSON", s);
            try
            {
                //解析從api傳回來的json字串
                JSONArray results = new JSONArray(s);

                for(int i=0; i<results.length(); i++)
                {
                    JSONObject waterPoint = results.getJSONObject(i);
                    JSONObject location = waterPoint.getJSONObject("location");
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
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //為了讓Toolbar的Menu有作用，這邊的程式不可以拿掉
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
