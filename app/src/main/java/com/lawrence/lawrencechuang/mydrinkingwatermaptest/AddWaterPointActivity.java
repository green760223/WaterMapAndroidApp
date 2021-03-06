package com.lawrence.lawrencechuang.mydrinkingwatermaptest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class AddWaterPointActivity extends AppCompatActivity
        implements RadialTimePickerDialogFragment.OnTimeSetListener,
        LocationListener
{

    private static final String FRAG_TAG_TIME_PICKER_START = "startTimePickerDialogFragment";
    private static final String FRAG_TAG_TIME_PICKER_CLOSE = "closeTimePickerDialogFragment2";
    private TextView openTime;
    private TextView closeTime;
    private int startHour, startMinute;
    private int closeHour, closeMinute;
    private String start;
    private String close;
    private LocationManager locationManager;
    private double longitude;
    private double latitude;
    private TextView results;
    private TextView results2;
    private String bestProvider;
    private Location location;
    private String coordinate;
    private String coordinateNet;
    private String coorGps;
    private String coorNet;
    private EditText waterName;
    private EditText waterDescription;
    private CheckBox warmWater;
    private CheckBox coldWater;
    private CheckBox hotWater;
    private CheckBox iceWater;
    private CheckBox allDay;
    private Spinner waterType;
    private Spinner beginTime;
    private Spinner endTime;
    private TextView waterStartClock;
    private TextView waterCloseClock;
    private String addWaterPointAPI;
    private RequestQueue queue;
    private String mWaterName;
    private String mWaterDescription;
    private String mWarmWater;
    private String mColdWater;
    private String mHotWater;
    private String mIceWater;
    private Boolean mAllDay;
    private String mBeginTime;
    private String mEndTime;
    private String mWaterType;
    private String mWaterStartClock;
    private String mWaterCloseClock;
    private String mOpenTime;
    private LocationManager locationManagerGps;
    private boolean isRemove = false;
    private CountDownTimer countDownTimer;
    private boolean isGps;
    private boolean isNetwork;
    private boolean isSuccess;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addWaterPointAPI = "http://drinkingwatermap-watermap.rhcloud.com/WaterMap/api/v1/waterPoints/addWaterPoint";
        setContentView(R.layout.activity_add_water_point);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("新增水點");
        findViews();

        results = (TextView) findViewById(R.id.test1);
        results2 = (TextView) findViewById(R.id.test2);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        showStartTimePickerDialog();
        showCloseTimePickerDialog();

//        Intent intent = new Intent();
//        intent.putExtra("code", "here is AddWaterPointActivity.class!");
//        setResult(RESULT_OK, intent);
//        if(isSuccess) {
//            setResultIntent();
//        }

    }

    public void getProvider()
    {
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            bestProvider = locationManager.GPS_PROVIDER;
        }
        else
        {
            bestProvider = locationManager.NETWORK_PROVIDER;
        }

//        results.setText(bestProvider);
    }


    /**
     * To know if user's done with add water point, then reload MainActivity.class when user pressed back button.
     */
    public void setResultIntent() {
        Intent intent = new Intent();
        intent.putExtra("code", "here is AddWaterPointActivity.class!");
        setResult(RESULT_OK, intent);
    }


    public void findViews()
    {
        waterName = (EditText) findViewById(R.id.ed_water_point_name);
        waterDescription = (EditText) findViewById(R.id.ed_water_point_description);
        warmWater = (CheckBox) findViewById(R.id.warm_water);
        coldWater = (CheckBox) findViewById(R.id.cold_water);
        hotWater = (CheckBox) findViewById(R.id.hot_water);
        iceWater = (CheckBox) findViewById(R.id.ice_water);
        allDay = (CheckBox) findViewById(R.id.all_day);
        waterType = (Spinner) findViewById(R.id.spinner_water_point_type);
        beginTime = (Spinner) findViewById(R.id.water_begintime);
        endTime = (Spinner) findViewById(R.id.water_endtime);
        waterStartClock = (TextView) findViewById(R.id.water_start);
        waterCloseClock = (TextView) findViewById(R.id.water_close);
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

//        locationManager.requestLocationUpdates(bestProvider, 3000, 0, this);
//        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 1000, 0, this);

        Log.d("=**NETWORK**=", isNetwork + "");
        Log.d("=**GPS**=", isGps + "");


        locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 1000, 0, this);
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 1000, 0, this);



//        if (coordinateNet != null) {
//            Log.d("=*network*=", coordinateNet);
//        } else if (coordinate != null) {
//            Log.d("=*gps*=", coordinate);
//        }

//        Log.d("=*gps*=", coordinate);
//        Log.d("=*network*=", coordinateNet);

//        countDownTimer = new CountDownTimer(120000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
////                Log.d("=countdown=", new SimpleDateFormat("s").format(millisUntilFinished));
//
//                if (coordinateNet != null) {
//                    Log.d("=*network*=", coordinateNet);
//                } else if (coordinate != null) {
//                    Log.d("=*gps*=", coordinate);
//                }
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        }.start();

//        Log.d("resBest", bestProvider + "");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("=Act2=", "destory");
//        countDownTimer.cancel();
    }


    public String showLocationInfoGps(Location location)
    {
        StringBuffer sb = new StringBuffer("");

        if (location != null) {
            sb.append(" 經度:" + location.getLongitude());
            sb.append(" 緯度:" + location.getLatitude());
            sb.append(" provider:" + LocationManager.GPS_PROVIDER);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.d("doubleLoc", longitude + "/" + latitude);
            Log.d("sb1:", location.getLongitude() + "/" + location.getLatitude() + "/" + LocationManager.GPS_PROVIDER);
        }

        coordinate = "(" + latitude + "," + longitude +")";

        results.setText(sb.toString());

        Log.d("coordinate", coordinate);
        Log.d("xxresultsxx", sb + "");

        return coordinate;
    }

    public String showLocationInfoNetWork(Location location)
    {
        StringBuffer sb = new StringBuffer("");

        if (location != null) {
            sb.append(" 經度:" + location.getLongitude());
            sb.append(" 緯度:" + location.getLatitude());
            sb.append(" provider:" + LocationManager.NETWORK_PROVIDER);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.d("doubleLoc", longitude + "/" + latitude);
            Log.d("sb1:", location.getLongitude() + "/" + location.getLatitude() + "/" + LocationManager.NETWORK_PROVIDER);
        }

        coordinateNet = "(" + latitude + "," + longitude +")";

        results2.setText(sb.toString());

        Log.d("coordinate", coordinateNet);
        Log.d("xxresultsxx", sb + "");

        return coordinateNet;
    }


    public void showStartTimePickerDialog()
    {
        Calendar c = Calendar.getInstance();
        startHour = c.get(Calendar.HOUR_OF_DAY);
        startMinute = c.get(Calendar.MINUTE);

        openTime = (TextView) findViewById(R.id.water_start);
        openTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                RadialTimePickerDialogFragment startTimePickerDialog = RadialTimePickerDialogFragment.newInstance(
                        AddWaterPointActivity.this
                        , startHour
                        , startMinute
                        , true);
                startTimePickerDialog.show(getSupportFragmentManager(), FRAG_TAG_TIME_PICKER_START);
            }

        });

    }


    public void showCloseTimePickerDialog() {
        Calendar c = Calendar.getInstance();
        closeHour = c.get(Calendar.HOUR_OF_DAY);
        closeMinute = c.get(Calendar.MINUTE);

        closeTime = (TextView) findViewById(R.id.water_close);
        closeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadialTimePickerDialogFragment startTimePickerDialog = RadialTimePickerDialogFragment.newInstance(
                        AddWaterPointActivity.this
                        , closeHour
                        , closeMinute
                        , true
                );
                startTimePickerDialog.show(getSupportFragmentManager(), FRAG_TAG_TIME_PICKER_CLOSE);
                startTimePickerDialog.getContext();

                Log.d("sssss", start + "");
            }
        });
    }


//    public void showTimePickerDialog()
//    {
//        final Calendar c = Calendar.getInstance();
//        mHour = c.get(Calendar.HOUR_OF_DAY);
//        mMinute = c.get(Calendar.MINUTE);
//
//        // 跳出時間選擇器
//        TimePickerDialog tpd = new TimePickerDialog(AddWaterPointActivity.this,
//                new TimePickerDialog.OnTimeSetListener()
//                {
//                    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
//                    {
//                        // 完成選擇，顯示時間
//
//                        openTime.setText(hourOfDay + ":" + minute);
//                    }
//                }, mHour, mMinute, false);
//        tpd.show();
//    }

    @Override
    public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute)
    {
        String mins = clockNumberCovert(String.valueOf(minute));
        String hrs = clockNumberCovert(String.valueOf(hourOfDay));

        if (dialog.getTag().equals("startTimePickerDialogFragment"))
        {
            openTime.setText("" + hrs + ":" + mins);
            //openTime.toString();
            //start = b + ":" + a;
            Log.d("ssss", openTime.getText() + "");
        } else {
            closeTime.setText("" + hrs + ":" + mins);
            //close = b + ":" + a;
            //Log.d("ccc", close + "");
        }

    }


    //新增水點
    public void addWaterPointInfo(View view)
    {
        mWaterName = waterName.getText().toString();
        mWaterDescription = waterDescription.getText().toString();
        mWarmWater = statusMapping(warmWater.isChecked());
        mColdWater = statusMapping(coldWater.isChecked());
        mHotWater = statusMapping(hotWater.isChecked());
        mIceWater = statusMapping(iceWater.isChecked());
        mAllDay = allDay.isChecked();
        mBeginTime = (String) beginTime.getSelectedItem();
        mEndTime = (String) endTime.getSelectedItem();
        mWaterType = (String) waterType.getSelectedItem();
        mWaterStartClock = (String) waterStartClock.getText();
        mWaterCloseClock = (String) waterCloseClock.getText();
        mOpenTime = mBeginTime + "至" + mEndTime + " " + mWaterStartClock + "-" + mWaterCloseClock;

        postStringRequest(addWaterPointAPI);

//        Toast.makeText(this, coorGps +
//                "/" + mWaterName +
//                "/" + mWaterDescription +
//                "/" + mWarmWater +
//                "/" + mHotWater +
//                "/" + mColdWater +
//                "/" + mIceWater +
//                "/" + mAllDay +
//                "/" + mOpenTime +
//                "/" + mWaterType
//                , Toast.LENGTH_LONG).show();
    }


    //透過API送出新增水點的資料到DB中
    public void postStringRequest(String apiUrl)
    {
        queue = Volley.newRequestQueue(this);
        JSONObject params = new JSONObject();
        isSuccess = false;

        try
        {
            params.put("waterPointName", mWaterName);
            params.put("description", mWaterDescription);
            if(coorGps != null && coorNet != null) {
                params.put("location", coorGps);
            } else {
                params.put("location", coorNet);
            }
            params.put("hotWater", mHotWater);
            params.put("coldWater", mColdWater);
            params.put("warmWater", mWarmWater);
            params.put("icedWater", mIceWater);
            params.put("waterPointTypes", mWaterType);
            params.put("openingHours", mOpenTime);

        } catch (JSONException e)
        {
            e.printStackTrace();
            Log.d("=fail=", "新增水點失敗！");
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Method.POST, apiUrl, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            String message = response.getString("status");
                            Log.d("messagexxx", message + "");
                            if(message.equals("0")) {
                                Toast.makeText(getApplicationContext(), "水點新增成功！", Toast.LENGTH_LONG).show();
                                isSuccess = true;

                                /*如果新增水點成功則傳回true要求返回首頁時重新整理以利看到使用者新增的水點位置*/
                                if(isSuccess) {
                                    setResultIntent();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.d("TAG", error.toString());
                    }
                });

        queue.add(jsonRequest);
    }


    public String statusMapping(Boolean status)
    {
        String mStatus;

        if(status)
        {
            mStatus = "1";
        }
        else
        {
            mStatus = "0";
        }

        return mStatus;
    }


    public String clockNumberCovert(String time)
    {
        String mTime = time;

        switch(mTime)
        {
            case "0":
                mTime = "00";
                break;
            case "1":
                mTime = "01";
                break;
            case "2":
                mTime = "02";
                break;
            case "3":
                mTime = "03";
                break;
            case "4":
                mTime = "04";
                break;
            case "5":
                mTime = "05";
                break;
            case "6":
                mTime = "06";
                break;
            case "7":
                mTime = "07";
                break;
            case "8":
                mTime = "08";
                break;
            case "9":
                mTime = "09";
                break;
        }

        return mTime;
    }

    /**
     * 判斷GPS是否開啟，GPS或者AGPS開啟一個就認為是開啟的
     */
    private boolean isGpsOpen() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //通過GPS衛星定位，定位級別可以精確到街（通過24顆衛星定位，在室外和空曠的地方定位準確、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        //通過WLAN或移動網路(3G/2G)確定的位置（也稱作AGPS，輔助GPS定位。主要用於在室內或遮蓋物（建築群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

        //只要其中一個開啟就回傳true
        if (gps == true || network == true) {
            return true;
        }

        return false;
    }

    /**
     * 詢問使用者是否要開啟gps
     */
    public void openGps() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(AddWaterPointActivity.this)
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
                            Toast.makeText(AddWaterPointActivity.this, "未開啟定位服務，無法使用本功能!", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        String provider = location.getProvider();
//        Log.d("=xPROVIDERx=", location.getProvider().toString());

        if (provider.equals("network")) {
            coorNet = showLocationInfoNetWork(location);
        } else if(provider.equals("gps")) {
            coorGps = showLocationInfoGps(location);
        }


//        coorGps = showLocationInfoGps(location);
//        coorNet = showLocationInfoNetWork(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
//        showLocationInfoGps(locationManager.getLastKnownLocation(provider));
    }

    @Override
    public void onProviderDisabled(String provider)
    {
//        showLocationInfoGps(null);
    }
}
