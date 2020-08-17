package com.archko.map.baidu_map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.*;

import org.json.JSONException;
import org.json.JSONObject;

public class AreaSelectorWithMapActivity extends Activity implements OnMapClickListener,
        OnGetGeoCoderResultListener {

    public static void start(Activity context) {
        context.startActivity(new Intent(context, AreaSelectorWithMapActivity.class));
    }

    View back;
    EditText map_address;
    View btn_save;
    View city_layout;
    TextView city_txt;
    //百度地图相关
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private GeoCoder mSearch;
    private ReverseGeoCodeOption mReverseGeoCodeOption;
    //百度地图SDK广播接受者判断key是否有效
    private SDKReceiver mReceiver;

    private boolean requestMoveCenter;

    ReverseGeoCodeResult geoCodeResult;
    LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInit.init(getApplicationContext());

        setContentView(R.layout.ac_area_selector_with_map);
        initializeView();
        initBaiduSDKReceiver();
    }

    private void initializeView() {
        back = findViewById(R.id.back);
        city_layout = findViewById(R.id.city_layout);
        city_txt = findViewById(R.id.city_txt);
        map_address = findViewById(R.id.address_txt);
        btn_save = findViewById(R.id.btn_save);
        mMapView = findViewById(R.id.mapview);
        mBaiduMap = mMapView.getMap();
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        mReverseGeoCodeOption = new ReverseGeoCodeOption();
        initBaiduMap();

        back.setOnClickListener(v -> {
            finish();
        });
        btn_save.setOnClickListener(v -> {
            if (null == geoCodeResult) {
                Toast.makeText(AreaSelectorWithMapActivity.this, "请移动地图选址", Toast.LENGTH_LONG).show();
            } else {
                String address = map_address.getText().toString();
                Intent intent = new Intent();
                JSONObject jo = new JSONObject();
                try {
                    jo.put("address", address);
                    jo.put("lat", geoCodeResult.getLocation().latitude);
                    jo.put("lng", geoCodeResult.getLocation().longitude);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                intent.putExtra("location", jo.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        city_layout.setOnClickListener(v -> {
            SelectCityActivity.start(AreaSelectorWithMapActivity.this);
        });
    }

    private void initBaiduMap() {
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(17.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                //移动地图中心的时候会产生循环调用
                if (!requestMoveCenter) {
                    if (mapStatus != null && mapStatus.bound != null) {
                        upDateMapLocation(mapStatus.bound.getCenter().latitude, mapStatus.bound.getCenter().longitude);
                    }
                }
            }
        });
    }

    @Override
    public void onMapClick(LatLng arg0) {
        upDateMapLocation(arg0.latitude, arg0.longitude);
    }

    @Override
    public void onMapPoiClick(MapPoi mp) {
        //点击地图上的建筑物事件回调
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        if (geoCodeResult != null && geoCodeResult.getLocation() != null) {
            upDateMapLocation(geoCodeResult.getLocation().latitude, geoCodeResult.getLocation().longitude);
        }
    }

    /**
     * 更新位置坐标.
     *
     * @param latitude
     * @param longitude
     */
    private void upDateMapLocation(double latitude, double longitude) {
        if (latitude == 0 || longitude == 0 || mSearch == null || mReverseGeoCodeOption == null) {
            return;
        }
        LatLng point = new LatLng(latitude, longitude);
        mSearch.reverseGeoCode(mReverseGeoCodeOption.location(point));
    }

    /**
     * 获取反地理信息查询结果，即坐标转地址.
     *
     * @param geoCodeResult
     * @see com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener#onGetReverseGeoCodeResult(com.baidu.mapapi.search.geocode.ReverseGeoCodeResult)
     */
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult geoCodeResult) {
        if (geoCodeResult.getLocation() == null || mBaiduMap == null) {
            return;
        }
        System.out.println("city:" + geoCodeResult.getCityCode());
        StringBuilder sb = new StringBuilder();
        sb.append("地址反查结果：[");
        sb.append(geoCodeResult.getLocation().latitude);
        sb.append(",");
        sb.append(geoCodeResult.getLocation().longitude);
        sb.append("],");
        sb.append("address:");
        sb.append(geoCodeResult.getAddress());
        sb.setLength(0);

        this.geoCodeResult = geoCodeResult;
        latLng = geoCodeResult.getLocation();
        updateBaiduMapCenterTip(geoCodeResult.getAddress(), geoCodeResult.getLocation());
    }

    //更新地图中心点的提示信息
    private void updateBaiduMapCenterTip(String tip, LatLng latLng) {
        if (null == mBaiduMap || TextUtils.isEmpty(tip) || null == latLng) {
            return;
        }
        map_address.setText(tip);
        mBaiduMap.clear();
        if (requestMoveCenter) {
            //移动地图中心点
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
            requestMoveCenter = false;
        }
        //百度地图提示TextView
        TextView mTipText = new TextView(getApplicationContext());
        mTipText.setBackgroundResource(R.drawable.bg_map_tip);
        mTipText.setText(tip);
        // 覆盖物(#addMapMarker)
        mBaiduMap.showInfoWindow(new InfoWindow(mTipText, latLng, -47));
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        try {
            if (mReceiver != null) {
                unregisterReceiver(mReceiver);
            }
        } catch (Exception e) {

        }
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onActivityResult(int reqeustCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (reqeustCode == SelectCityActivity.REQUEST_CODE) {
                City city = (City) data.getSerializableExtra(SelectCityActivity.EXTRA_KEY_OUT);
                afterSelectCity(city);
            }
        }
    }

    private void afterSelectCity(City city) {
        if (city != null) {
            city_txt.setText(city.name);
            if (mSearch != null) {
                requestMoveCenter = true;
                GeoCodeOption geo = new GeoCodeOption();
                geo.address(city.name);
                geo.city(city.name);
                mSearch.geocode(geo);
            }
        }
    }

    /**
     * 百度构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    private void initBaiduSDKReceiver() {
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);
    }

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
            }
        }
    }
}
