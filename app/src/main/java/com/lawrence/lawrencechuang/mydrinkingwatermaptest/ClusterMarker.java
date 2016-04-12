package com.lawrence.lawrencechuang.mydrinkingwatermaptest;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by LawrenceChuang on 16/4/1.
 */
public class ClusterMarker implements ClusterItem {
    private LatLng position;
    private String mName;
    private int markerIcon;
    private String mDescription;
    private String openTime;
    private String mHotWater;
    private String mWarmWater;
    private String mColdWater;
    private String mIceWater;

    public ClusterMarker(double lat, double lng, String name, int icon, String description, String time,
                         String hotWater, String warmWater, String coldWater, String iceWater) {
        position = new LatLng(lat, lng);
        mName = name;
        markerIcon = icon;
        mDescription = description;
        openTime = time;
        mHotWater = hotWater;
        mWarmWater = warmWater;
        mColdWater = coldWater;
        mIceWater = iceWater;
    }

    @Override
    public LatLng getPosition() {

        return position;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public int getMarkerIcon() {
        return markerIcon;
    }

    public void setMarkerIcon(int markerIcon) {
        this.markerIcon = markerIcon;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getmHotWater() {
        return mHotWater;
    }

    public void setmHotWater(String mHotWater) {
        this.mHotWater = mHotWater;
    }

    public String getmWarmWater() {
        return mWarmWater;
    }

    public void setmWarmWater(String mWarmWater) {
        this.mWarmWater = mWarmWater;
    }

    public String getmColdWater() {
        return mColdWater;
    }

    public void setmColdWater(String mColdWater) {
        this.mColdWater = mColdWater;
    }

    public String getmIceWater() {
        return mIceWater;
    }

    public void setmIceWater(String mIceWater) {
        this.mIceWater = mIceWater;
    }
}
