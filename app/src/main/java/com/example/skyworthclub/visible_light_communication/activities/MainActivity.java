package com.example.skyworthclub.visible_light_communication.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.example.skyworthclub.visible_light_communication.R;
import com.example.skyworthclub.visible_light_communication.adapters.SearchAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity  implements LocationSource, AMapLocationListener,
        TextWatcher, AdapterView.OnItemClickListener, Inputtips.InputtipsListener{

    private MapView mMapView;
    //初始化地图控制器对象
    private AMap aMap;
    private CameraUpdate cameraUpdate;

    private EditText editText;
    private ListView listView;
    private TextView textView;
    private SearchAdapter searchAdapter;
    List<HashMap<String,String>> searchList = new ArrayList<HashMap<String, String>>();;
    private String currentCity;


    OnLocationChangedListener onLocationChangedListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    //经纬度地点依次是正佳，天河城，太古汇
    private double[] position = {23.1323070000,113.3270370000,23.1322190000,113.3226170000,23.1342510000,113.3324550000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xyj_main);

        init();//初始化

//        System.out.println("你麻痹"+ sHA1(this));
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        // 设置定位监听，必须放在前面才能实现监听
        aMap.setLocationSource(this);
        MyLocationStyle myLocationStyle;
        //初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        // 连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）
        // 如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle = new MyLocationStyle();
        //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
//        myLocationStyle.interval(4000);

        //定位一次，且将视角移动到地图中心点。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);

        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMyLocationEnabled(true);

        //隐藏左下角的"高德地图"logo
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setLogoBottomMargin(-50);
//        uiSettings.setScaleControlsEnabled(true);
//        Log.e("TAG", "缩放功能"+uiSettings.isScaleControlsEnabled()+"");

//        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        getAdress(position[0], position[1]);
        //getAdress(position[2], position[3]);
        //getAdress(position[4],position[5]);

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //存放drawableLeft，Right，Top，Bottom四个图片资源对象
                //index=2 表示的是 drawableRight 图片资源对象
                Drawable drawable = editText.getCompoundDrawables()[2];
                if (drawable == null)
                    return false;
                if (event.getAction() == MotionEvent.ACTION_UP){
                    if (event.getX() > editText.getWidth()-editText.getPaddingRight()-drawable.getIntrinsicWidth()){

//                        Log.e("TAG","为什么有进来了");
                        if (editText.getText().toString() != null){
                            editText.clearFocus();
                            editText.setText("");
                            searchList.clear();
                            searchAdapter.notifyDataSetChanged();
                        }

                    }
                    return false;
                }
                return false;
            }
        });

    }


    private void init(){
        mMapView = (MapView) findViewById(R.id.map);
        editText = (EditText)findViewById(R.id.xyj_editText);
        listView = (ListView)findViewById(R.id.xyj_listView);
        textView = (TextView)findViewById(R.id.xyj_currentCity);
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        textView.setText("无网络");

        editText.addTextChangedListener(this);
        listView.setOnItemClickListener(this);
    }


    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {

//        System.out.println("激活定位");
        onLocationChangedListener = listener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        onLocationChangedListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {

//        System.out.println("开始工作");
        if (onLocationChangedListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                // 显示系统小蓝点
                onLocationChangedListener.onLocationChanged(amapLocation);
                //获取当前城市
                currentCity = amapLocation.getCity();
                textView.setText(currentCity);
                Log.e("TAG","当前城市："+currentCity);
                Log.e("TAG",""+searchList.size()+"");
                Log.e("TAG", "editText的大小"+editText.getText().toString());

            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }


    /*
    解释指定坐标的地址
    @param x 经度
    @param y 纬度
     */
    public void getAdress(final double x, final double y){
//        Log.e("TAG", "调用getAdress");
        //地址查询器
        GeocodeSearch geocodeSearch = new GeocodeSearch(this);
        //设置查询参数,
        //三个参数依次为坐标，范围多少米，坐标系
        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(new LatLonPoint(x, y), 200, GeocodeSearch.AMAP);
        //设置查询结果监听
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            //根据坐标获取地址信息调用
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                String result = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                Log.e("TAG","获得请求结果");
                makepoint(x, y, result);
            }
            //根据地址获取坐标信息是调用
            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
        //发起异步查询请求
        geocodeSearch.getFromLocationAsyn(regeocodeQuery);

    }

    /*
    在地图上绘制相应的点
    @param x       经度
    @param y       纬度
    @param result  地点名称
     */
    public void makepoint(double x, double y, String result){
        Log.e("TAG","开始绘图");
        //北纬39.22，东经116.39，为负则表示相反方向
        LatLng latLng=new LatLng(x, y);
        Log.e("地址",result);

        //使用默认点标记
        Marker marker = aMap.addMarker(new MarkerOptions().position(latLng).title(x+"").snippet(result));
        //改变可视区域为指定位置
        //CameraPosition4个参数分别为位置，缩放级别，目标可视区域倾斜度，可视区域指向方向（正北逆时针算起，0-360）
        cameraUpdate= CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng,14,0,30));
        aMap.moveCamera(cameraUpdate);//地图移向指定区域

        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String temp = marker + "";
                Log.e("TAG", "marker的标题："+marker.getPosition()+"大小："+temp.length());
//                Toast.makeText(MainActivity.this,"点击指定位置",Toast.LENGTH_SHORT).show();
                if (marker.getTitle().equals("23.132307")){
//                    Log.e("TAG", "大家好，我进来了");
                    //跳转界面二
                    Intent intent = new Intent(MainActivity.this, PagetwoActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                return false;//false 点击marker marker会移动到地图中心，true则不会
            }
        });

        aMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(MainActivity.this,"点击了InfoWindow，如需跳转请点击下方图标",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.e("TAG", "editText的内容改变了");
        //获取自动提示输入框的内容
        String content = s.toString().trim();

        //初始化一个输入提示搜索对象，并传入参数
        InputtipsQuery inputtipsQuery = new InputtipsQuery(content,currentCity);
        //将获取到的结果进行城市限制筛选
        inputtipsQuery.setCityLimit(true);
        //定义一个输入提示对象，传入当前上下文和搜索对象
        Inputtips inputtips=new Inputtips(this,inputtipsQuery);
        //设置输入提示查询的监听，实现输入提示的监听方法onGetInputtips()
        inputtips.setInputtipsListener(this);
        //输入查询提示的异步接口实现
        inputtips.requestInputtipsAsyn();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onGetInputtips(List<Tip> list, int returnCode) {
        //如果输入提示搜索成功
        if(returnCode == AMapException.CODE_AMAP_SUCCESS){
            //每次搜索时都先把原来的searchList内容清掉
            searchList.clear();
            for (int i=0;i<list.size();i++){
                HashMap<String,String> hashMap = new HashMap<String, String>();
                hashMap.put("name",list.get(i).getName());
                //将地址信息取出放入HashMap中
                hashMap.put("address",list.get(i).getDistrict());
                Log.e("TAG", list.get(i).getPoint().toString());
                //解析返回的经纬度
                String latlonPoint = list.get(i).getPoint().toString();
                //经度
                String x = latlonPoint.substring(0, latlonPoint.indexOf(","));
                //纬度
                String y = latlonPoint.substring(latlonPoint.indexOf(",")+1, latlonPoint.length());
                //详细地址
                String detailAddress = list.get(i).getAddress();
                hashMap.put("x", x);
                hashMap.put("y", y);
                hashMap.put("detailAddress", detailAddress);
                //将HashMap放入表中
                searchList.add(hashMap);

            }
            //新建一个适配器
            searchAdapter = new SearchAdapter(this, searchList);
            //为listview适配
            listView.setAdapter(searchAdapter);

        }else{
            //清空原来的所有item
            searchList.clear();
            searchAdapter.notifyDataSetChanged();
            Log.e("TAG", "editText内容为空时返回的错误返回码:"+returnCode);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        double x = Double.parseDouble(searchList.get(position).get("x"));
        double y = Double.parseDouble(searchList.get(position).get("y"));
        String detailAddress = searchList.get(position).get("detailAddress");
        Log.e("TAG","点击listView地点的经度:"+x+"   纬度:"+y);
        //在地图上显示我点击的地点
        makepoint(x, y, detailAddress);
        //把listView清空
        searchList.clear();
//        Log.e("TAG", "searchList的大小："+searchList.size());
        searchAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        if(null != mlocationClient){
            mlocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        Log.e("TAG", "重新开始");
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        Log.e("TAG", "进入下一个界面，暂停");
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }
}
