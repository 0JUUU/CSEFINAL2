package com.youngju.csefinal2.Interface;

import com.youngju.csefinal2.MyLatLng;

import java.util.List;

public interface IOnLoadLocationListener {
    void onLoadLocationSuccess(List<MyLatLng> latLngs);
    void onLoadLocationFailed(String message);
}
