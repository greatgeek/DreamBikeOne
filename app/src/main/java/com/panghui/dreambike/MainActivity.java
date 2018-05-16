package com.panghui.dreambike;

import android.Manifest;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
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
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.panghui.dreambike.Util.HttpUtil;
import com.panghui.dreambike.Util.ToastUtil;
import com.panghui.dreambike.Util.TripRecordItem;
import com.panghui.dreambike.Util.User;
import com.panghui.dreambike.base.AppConst;
import com.panghui.dreambike.dialog.LoadDialog;
import com.panghui.dreambike.dialog.MyLoadDialog;
import com.panghui.dreambike.overlay.RideRouteOverlay;
import com.panghui.dreambike.overlay.WalkRouteOverlay;
import com.panghui.dreambike.route.RideRouteDetailActivity;
import com.panghui.dreambike.route.WalkRouteDetailActivity;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements AMap.OnMyLocationChangeListener
        ,AMap.OnMapTouchListener,AMap.OnMarkerClickListener,AMap.InfoWindowAdapter
        ,RouteSearch.OnRouteSearchListener,AMap.OnInfoWindowClickListener,GeocodeSearch.OnGeocodeSearchListener{
    private static final String TAG = "MainActivity";
    /**Login部分变量*/
    private DrawerLayout mDrawerLayout;/**抽屉*/
    private CircleImageView Login_imageView;
    private TextView Login_mail;
    private TextView Login_username;
    private boolean isLogin=false;//判断是否登录
    /**广播部分*/
    private IntentFilter intentFilter;
    private LocalBroadcastReceiver localBroadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;
    /**地图部分*/
    MapView mMapView=null;
    AMap aMap=null;
    MyLocationStyle myLocationStyle;
    LatLonPoint mStartPoint;//我的位置坐标
    LatLonPoint mEndPoint;//终点坐标
    private WalkRouteResult mWalkRouteResult;
    private WalkRouteOverlay mWalkRouteOverlay;
    private RideRouteResult mRideRouteResult;
    private RideRouteOverlay mRideRouteOverlay;
    private WalkPath walkPath;
    private RidePath ridePath;
    private Marker tempMarker;/**用于点击显示点的傀儡Marker*/
    private String timeMin;
    private String timeSec;
    private String distance;

    boolean isFirstLocat=true;
    private int rideOverlayCount=0;
    private int walkOverlayCount=0;
    private RouteSearch mRouteSearch;
    private final int ROUTE_TYPE_WALK=3;
    private final int ROUTE_TYPE_RIDE=4;
    private int goOutChoice=ROUTE_TYPE_WALK;
    private final int REQUEST_ROUTEDETAIL=1;
    private final int REQUEST_SCANQR=2;


    /**五个图标*/
    private ImageView refresh;
    private ImageView search_iv;
    private ImageView scancode_iv;
    private ImageView endTrip;
    private ProgressBar loadingbar;
    private ImageView man_iv;
    /**marker数据*/
    String markerlocjsonData=null;
    String markerUrl="http://120.79.91.50/locationTojson.php";
    /**二维码扫描部分*/
    private String QRresult=null;//此QRresult即是单车ID
    final String blueToothUrl="http://120.79.91.50/DreamBike/DreamBike_bluetoothlockMaster.php";
    /**出行记录,扣费情况**/
    private String tripRecordjsonData;
    private int tripRecord_fakeid;//出行记录id
    //TODO
    private String spendmoney="10";
    final String tripRecordUrl="http://120.79.91.50/DreamBike/DreamBike_triprecord.php";
    final String createTripRecordUrl="http://120.79.91.50/DreamBike/DreamBike_createtriprecord.php";
    final String updateTripRecordUrl="http://120.79.91.50/DreamBike/DreamBike_updatetriprecord.php";
    final String timeDiffUrl="http://120.79.91.50/DreamBike/DreamBike_timedifference.php";
    final String lockTheBikeUrl="http://120.79.91.50/DreamBike/DreamBike_bluetoothlockbike.php";
    /**逆地理编码*/
    private GeocodeSearch geocodeSearch;
    private String addressName;

    /**上下文*/
    private Context mContext;
    private Resources mResources;

    /**用户属性*/
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**获取上下文*/
        mContext=getApplicationContext();
        mResources=getResources();

        /**Login部分初始化*/
        mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        NavigationView navView=(NavigationView)findViewById(R.id.nav_view);
        View headerView=navView.inflateHeaderView(R.layout.nav_header);
        Login_mail=(TextView)headerView.findViewById(R.id.mail);
        Login_username=(TextView)headerView.findViewById(R.id.username);
        Login_imageView=(CircleImageView)headerView.findViewById(R.id.icon_image);
        /**注册本地广播监听器*/
        localBroadcastManager=LocalBroadcastManager.getInstance(this);
        intentFilter=new IntentFilter();
        intentFilter.addAction("com.panghui.dreambike.LOCAL_BROADCAST");
        localBroadcastReceiver=new LocalBroadcastReceiver();
        localBroadcastManager.registerReceiver(localBroadcastReceiver,intentFilter);
        /**地图部分*/
        mMapView=(MapView)findViewById(R.id.map);
        refresh=(ImageView)findViewById(R.id.iv_refresh);
        search_iv=(ImageView)findViewById(R.id.iv_search);
        scancode_iv=(ImageView)findViewById(R.id.iv_scan_code);
        endTrip=(ImageView)findViewById(R.id.endTrip);
        man_iv=(ImageView)findViewById(R.id.man);
        mMapView.onCreate(savedInstanceState);
        /**加载圈*/
        loadingbar=(ProgressBar)findViewById(R.id.loading_bar);
        /**用户属性*/
        user=User.getInstance();

        /**二维码扫描部分*/
        ZXingLibrary.initDisplayOpinion(this);

        final Animation circle_anim= AnimationUtils.loadAnimation(this,R.anim.refresh_rotate);//获取旋转资源
        LinearInterpolator interpolator=new LinearInterpolator();//设置匀速旋转
        circle_anim.setInterpolator(interpolator);//旋转资源设置为匀速旋转

        init();
        List<String> permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            //TODO
            Mylocation();
        }
        new GetMarker().execute(markerUrl);
        showMyDialog();
        /**抽屉头像点击事件*/
        Login_imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        /**刷新按钮点击事件*/
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetMarker().execute(markerUrl);
                refresh.startAnimation(circle_anim);
                getAddress(mStartPoint);
                showMyDialog();
                Zero_operation();
            }
        });
        /**搜索按钮点击事件*/
        search_iv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, InputtipsActivity.class);
                startActivityForResult(intent,REQUEST_ROUTEDETAIL);
            }
        });
        /**二维码扫描按钮点击事件*/
        scancode_iv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isLogin){
                    Toast.makeText(MainActivity.this, "请登录后使用扫码功能！", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent=new Intent(MainActivity.this, CaptureActivity.class);
                    startActivityForResult(intent,REQUEST_SCANQR);
                }
            }
        });
        /**小人物的点击事件*/
        man_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        /**结束行程点击事件*///TODO
        endTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lat = new Double(mStartPoint.getLatitude()).toString();
                String lon = new Double(mStartPoint.getLongitude()).toString();
                /**插入骑行记录终点坐标*/
                HttpUtil.updateTripRecord(mhandler,updateTripRecordUrl,tripRecord_fakeid,user.getEmail(),lat,lon);
                HttpUtil.lockTheBike(mhandler,lockTheBikeUrl,QRresult);
            }
        });

        //TODO 未完成的点击事件
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_triprecord:
                        if (isLogin==false){
                            Toast.makeText(MainActivity.this,"请登录后再查看骑行记录！",Toast.LENGTH_SHORT).show();
                        }else{
                            Intent intent=new Intent(MainActivity.this,TripRecord.class);
                            intent.putExtra("email",Login_mail.getText().toString().trim());
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                        }

                        break;
                    case R.id.nav_wallet:
                        if (isLogin==false){
                            Toast.makeText(MainActivity.this,"请登录后再查看钱包！",Toast.LENGTH_SHORT).show();
                        }else{
                            Intent intent=new Intent(MainActivity.this,WalletActivity.class);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                        }
                        break;
                    case R.id.nav_location:
                        getAddress(mStartPoint);
                        break;
                    case R.id.nav_owner:
                        item.setIcon(R.drawable.nav_owner);
                        Intent intent=new Intent(MainActivity.this,DreamBikeOwnerActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                        break;
                    default:
                }
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result:grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    //TODO
                    //new GetMarker().execute();
                }else{
                    Toast.makeText(this, "发生未知错误！", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
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
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localBroadcastReceiver);
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_ROUTEDETAIL:
                if (resultCode==RESULT_OK){
                    String returnLatLng=data.getStringExtra("LatLng");
                    String[] Latlngstr=returnLatLng.split(",");
                    double lat=Double.parseDouble(Latlngstr[0]);
                    double lng=Double.parseDouble(Latlngstr[1]);

                    LatLng latLng=new LatLng(lat,lng);
                    final Marker marker=aMap.addMarker(new MarkerOptions().position(latLng));
                    drawRouteToGo(marker);
                }
                break;
            case REQUEST_SCANQR:
                if (data!=null){
                    Bundle bundle=data.getExtras();
                    if (bundle==null){
                        return;
                    }
                    if (bundle.getInt(CodeUtils.RESULT_TYPE)==CodeUtils.RESULT_SUCCESS){
                        QRresult=bundle.getString(CodeUtils.RESULT_STRING);
                        Toast.makeText(this,"解析结果："+QRresult,Toast.LENGTH_LONG).show();
                        new Modifybluetoothlock().execute(blueToothUrl);/**解锁单车*/
                    }else if (bundle.getInt(CodeUtils.RESULT_TYPE)==CodeUtils.RESULT_FAILED){
                        Toast.makeText(this,"解析二维码失败",Toast.LENGTH_LONG).show();
                    }
                }
        }
    }
    /*******************************function define*******************************************************************/
    /**归零操作*/
    private void Zero_operation(){
        rideOverlayCount=0;
        walkOverlayCount=0;
    }
    /**初始化AMap对象*/
    private void init(){
        if (aMap==null){
            aMap=mMapView.getMap();
            aMap.moveCamera(CameraUpdateFactory.zoomTo(15));//高德地图初始化后设置的缩放级别
            aMap.getUiSettings().setZoomControlsEnabled(false);//取消绽放按钮

            mRouteSearch=new RouteSearch(this);
            mRouteSearch.setRouteSearchListener(this);
            geocodeSearch=new GeocodeSearch(this);
            geocodeSearch.setOnGeocodeSearchListener(this);
        }
        registerListener();
    }

    /**注册监听*/
    private void registerListener(){
        mMapView.getMap().setOnMyLocationChangeListener(this);//非常重要，设置此监听器即可监听变化，加入控制事件
        aMap.setOnMapTouchListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setInfoWindowAdapter(this);
        aMap.setOnInfoWindowClickListener(this);
    }
    /**控制事件*/
    void Mylocation(){
        if (isFirstLocat){
            myLocationStyle=new MyLocationStyle();
            myLocationStyle.interval(2000)
                    .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
                    .showMyLocation(true);
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setMyLocationEnabled(true);
            isFirstLocat=false;
        }
    }
    /**显示加载框*/
    private void showDialog(){
        LoadDialog loadDialog=LoadDialog.getInstance();
        loadDialog.setStyle(DialogFragment.STYLE_NORMAL,R.style.load_dialog);
        LoadDialog.getInstance().show(getSupportFragmentManager(),"");
    }
    private void showMyDialog(){
        MyLoadDialog myLoadDialog=MyLoadDialog.getInstance();
        myLoadDialog.setStyle(MyLoadDialog.STYLE_NORMAL,R.style.load_dialog);
        MyLoadDialog.getInstance().show(getSupportFragmentManager(),"");
    }
    private void followMyLocation(){
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000)
                .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
                .showMyLocation(true);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
    }
    private void notfollowMyLocation(){
        myLocationStyle=new MyLocationStyle();
        myLocationStyle.interval(2000)
                .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                .showMyLocation(true);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
    }
    /**响应逆地理编码*/
    public void getAddress(final LatLonPoint latLonPoint){
        RegeocodeQuery query=new RegeocodeQuery(latLonPoint,200,
                GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
    }

    /**开始搜索路径规划方案*/
    public void searchRouteResult(int routeType,int mode){
        if (mStartPoint==null){
            Toast.makeText(this,"定位中，稍后再试...",Toast.LENGTH_LONG).show();
            return;
        }
        if (mEndPoint==null){
            Toast.makeText(this,"终点未设置!",Toast.LENGTH_LONG).show();
            return;
        }
        showDialog();
        final RouteSearch.FromAndTo fromAndTo=new RouteSearch.FromAndTo(mStartPoint,mEndPoint);
        if (routeType==ROUTE_TYPE_WALK){
            RouteSearch.WalkRouteQuery query=new RouteSearch.WalkRouteQuery(fromAndTo);
            mRouteSearch.calculateWalkRouteAsyn(query);
        }
    }
    /**骑行路线规划*/
    public void searchRideRouteResult(int routeType,int mode){
        if (mStartPoint==null){
            Toast.makeText(this,"定位中，稍后再试...",Toast.LENGTH_LONG).show();
            return;
        }
        if (mEndPoint==null){
            Toast.makeText(this,"终点未设置！",Toast.LENGTH_LONG).show();
        }
        /*****/
        final RouteSearch.FromAndTo fromAndTo=new RouteSearch.FromAndTo(mStartPoint,mEndPoint);
        if (routeType==ROUTE_TYPE_RIDE){
            RouteSearch.RideRouteQuery query=new RouteSearch.RideRouteQuery(fromAndTo);
            mRouteSearch.calculateRideRouteAsyn(query);
        }
    }

    public void drawRouteToGo(final Marker marker){
        mEndPoint=new LatLonPoint(marker.getPosition().latitude,marker.getPosition().longitude);
        if (tempMarker!=null){/**说明傀儡Marker已经指向一个Marker，有一个步行图层*/
            tempMarker=null;/**将傀儡Marker指向null*/
            //mWalkRouteOverlay.removeFromMap();/**去除上一点的图层*/
            //mRideRouteOverlay.removeFromMap();
        }
        if (rideOverlayCount>0){
            mRideRouteOverlay.removeFromMap();
            rideOverlayCount=0;
        }
        if (walkOverlayCount>0){
            mWalkRouteOverlay.removeFromMap();
            walkOverlayCount=0;
        }
        searchRideRouteResult(ROUTE_TYPE_RIDE,RouteSearch.RIDING_DEFAULT);/**发起请求*/
        tempMarker=marker;

        ToastUtil.show(this,"路线规划已经完成！");
    }

    /**自定义infowindow窗口**/
    public void render(Marker marker,View view){
        TextView tv_time=(TextView)view.findViewById(R.id.tv_time_min);
        TextView tv_time_info=(TextView)view.findViewById(R.id.tv_time_sec);
        TextView tv_distance=(TextView)view.findViewById(R.id.tv_distance);
        TextView tv_tips=(TextView)view.findViewById(R.id.tips_tv);
        if (goOutChoice==ROUTE_TYPE_WALK){
            tv_tips.setText("步行：");
        }
        if (goOutChoice==ROUTE_TYPE_RIDE){
            tv_tips.setText("骑行：");
        }
        tv_time.setText(timeMin+"min");
        tv_time_info.setText(timeSec+"sec");
        tv_distance.setText(distance+"m");
    }

    @Override
    public void onMyLocationChange(Location location) {
        mStartPoint=new LatLonPoint(location.getLatitude(),location.getLongitude());
        Mylocation();
    }

    @Override
    public void onTouch(MotionEvent motionEvent) {
        notfollowMyLocation();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        mEndPoint=new LatLonPoint(marker.getPosition().latitude,marker.getPosition().longitude);
        if (tempMarker!=null){/**说明傀儡Marker已经指向一个Marker*/
            tempMarker=null;/**将傀儡Marker指向一个null*/
            mWalkRouteOverlay.removeFromMap();/**去除上一点的涂层*/
            //mRideRouteOverlay.removeFromMap();
        }
        if (rideOverlayCount>0){/**如果骑行图层存在，则清除它*/
            mRideRouteOverlay.removeFromMap();
            rideOverlayCount=0;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchRouteResult(ROUTE_TYPE_WALK,RouteSearch.WALK_DEFAULT);/**点击后，发起请求*/
                tempMarker=marker;/**傀儡Marker指向该Marker，即点击的那个Marker*/
            }
        }).start();
        return true;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        Log.d(TAG,"getInfoWindow");
        View infoWindow=getLayoutInflater().inflate(R.layout.info_window,null);
        render(marker,infoWindow);
        return infoWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        Log.d(TAG,"getInfoContents");
        return null;
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }
    /**步行方案*/
    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        LoadDialog.getInstance().dismiss();
        if (errorCode== AMapException.CODE_AMAP_SUCCESS){
            if (result!=null&&result.getPaths()!=null){
                if (result.getPaths().size()>0){
                    mWalkRouteResult=result;
                    walkPath=mWalkRouteResult.getPaths().get(0);
                    mWalkRouteOverlay=new WalkRouteOverlay(this,aMap,walkPath,
                            mWalkRouteResult.getStartPos(),mWalkRouteResult.getTargetPos());
                    mWalkRouteOverlay.removeFromMap();
                    mWalkRouteOverlay.addToMap();
                    mWalkRouteOverlay.zoomToSpan();
                    int dis=(int)walkPath.getDistance();
                    int dur=(int)walkPath.getDuration();
                    timeMin=new Integer(dur/60).toString();
                    timeSec=new Integer(dur%60).toString();
                    distance=new Integer(dis).toString();
                    goOutChoice=ROUTE_TYPE_WALK;
                    tempMarker.showInfoWindow();/**发起请求后，等待onWalkRouteSearched()这个回调接口
                                                    的调用，此次调用成功即说明数据接收和解析成功，再显示InfoWindow信息窗口*/
                    walkOverlayCount++;
                }else if (result!=null&&result.getPaths()==null){
                    Toast.makeText(this,"对不起，没有搜索到相关数据！",Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(this,"对不起，没有搜索到相关数据!",Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this,errorCode,Toast.LENGTH_LONG).show();
        }
    }
    /**骑行方案*/
    @Override
    public void onRideRouteSearched(RideRouteResult result, int errorCode) {
        if (errorCode==AMapException.CODE_AMAP_SUCCESS){
            if (result!=null&&result.getPaths()!=null){
                if (result.getPaths().size()>0){
                    mRideRouteResult=result;
                    ridePath=mRideRouteResult.getPaths().get(0);
                    mRideRouteOverlay=new RideRouteOverlay(this,aMap,ridePath,
                            mRideRouteResult.getStartPos(),mRideRouteResult.getTargetPos());
                    mRideRouteOverlay.removeFromMap();
                    mRideRouteOverlay.addToMap();
                    mRideRouteOverlay.zoomToSpan();
                    int dis=(int)ridePath.getDistance();
                    int dur=(int)ridePath.getDuration();
                    timeMin=new Integer(dur/60).toString();
                    timeSec=new Integer(dur%60).toString();
                    distance=new Integer(dis).toString();
                    goOutChoice=ROUTE_TYPE_RIDE;
                    tempMarker.showInfoWindow();
                    rideOverlayCount++;/**绘制完图层后，骑行图层+1*/
                }else if (result!=null&&result.getPaths()==null){
                    Toast.makeText(this,"对不起，没有搜索到相关数据！",Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(this,"对不起，没有搜索到相关数据！",Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this,errorCode,Toast.LENGTH_LONG).show();
        }
    }
    /**Info窗口点击事件，若是步行则启动步行细节Activity,若是骑行则启动骑行细节Activity*/
    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent;
        if (goOutChoice==ROUTE_TYPE_WALK){
            intent=new Intent(MainActivity.this, WalkRouteDetailActivity.class);
            intent.putExtra("walk_path",walkPath);
            intent.putExtra("walk_result",mWalkRouteResult);
        }else{
            intent=new Intent(MainActivity.this,RideRouteDetailActivity.class);
            intent.putExtra("ride_path",ridePath);
            intent.putExtra("ride_result",mRideRouteResult);
        }
        startActivity(intent);
    }
    /**逆地理编码*/
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode==AMapException.CODE_AMAP_SUCCESS){
            if (result!=null&&result.getRegeocodeAddress()!=null
                    &&result.getRegeocodeAddress().getFormatAddress()!=null){
                addressName=result.getRegeocodeAddress().getCity();//仅获取城市名称
                ToastUtil.show(MainActivity.this,addressName);
            }else{
                ToastUtil.show(MainActivity.this,"对不起，没有搜索到相关数据！");
            }
        }else{
            ToastUtil.showerror(this,rCode);
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }
    /******************************class define****************************************************************/
    /**继承BroadcastReceiver类重写onReceive()函数，实现想要的功能*/
    class LocalBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            Login_username.setText(user.getUsername());
            Login_mail.setText(user.getEmail());
            isLogin=true;//表示已经成功登录
            new getTripRecord().execute(tripRecordUrl);//TODO
            Toast.makeText(MainActivity.this,"登录成功！",Toast.LENGTH_LONG).show();
        }
    }

    class GetMarker extends AsyncTask<String ,Void,String>{
        @Override
        protected String doInBackground(String... urls) {
            String responseData=null;
            try{
                OkHttpClient client=new OkHttpClient();
                Request request=new Request.Builder()
                        .url(urls[0])
                        .build();
                Response response=client.newCall(request).execute();
                if (response.code()==200){
                    responseData=response.body().string();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return responseData;
        }

        @Override
        protected void onPostExecute(String responseData) {
            markerlocjsonData=responseData;
            if (markerlocjsonData!=null){
                ArrayList<MarkerOptions> markerOptions= HttpUtil.MarkerparseJSONWithJSONObject(markerlocjsonData);
                aMap.clear(true);
                aMap.addMarkers(markerOptions,true);
                aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
                refresh.clearAnimation();//数据加载完成后，停止转动
                followMyLocation();
                MyLoadDialog.getInstance().dismiss();
            }
        }
    }

    public class Modifybluetoothlock extends AsyncTask<String,Void,String>{
        @Override
        protected void onPreExecute() {
            loadingbar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... urls) {
            String result=null;
            try{
                OkHttpClient client=new OkHttpClient();
                RequestBody requestBody=new FormBody.Builder()
                        .add("bikeID",QRresult)
                        .build();
                Request request=new Request.Builder()
                        .url(urls[0])
                        .post(requestBody)
                        .build();
                Response response=client.newCall(request).execute();
                if (response.code()==200){
                    result=response.body().string();
                    //TODO 创建骑行记录并插入起始坐标
                    if (result.trim().equals("megneto")){//在PHP脚本中，若是成功更新了状态则会返回megneto，其他情况不会返回此值,此更新即为解锁
                        String lat=new Double(mStartPoint.getLatitude()).toString();
                        String lon=new Double(mStartPoint.getLongitude()).toString();
                        HttpUtil.createTripRecord(mhandler,createTripRecordUrl,tripRecord_fakeid,user.getEmail(),lat,lon);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            loadingbar.setVisibility(View.INVISIBLE);
            //Toast.makeText(MainActivity.this, result+"解锁成功！", Toast.LENGTH_SHORT).show();
        }
    }

    public class getTripRecord extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... urls) {
            String jsonData=null;
            try{
                OkHttpClient client=new OkHttpClient();
                RequestBody requestBody=new FormBody.Builder()
                        .add("email",Login_mail.getText().toString().trim())
                        .build();
                Request request=new Request.Builder()
                        .url(urls[0])
                        .post(requestBody)
                        .build();
                Response response=client.newCall(request).execute();
                if (response.code()==200){
                    jsonData=response.body().string();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return jsonData;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            tripRecordjsonData=jsonData;
            List<TripRecordItem> tripRecord = HttpUtil.itemparseJSONWithJSONObjec(tripRecordjsonData);
            tripRecord_fakeid=tripRecord.size()+1;//获得出行记录id
            //Toast.makeText(MainActivity.this,"骑行记录加载完成！",Toast.LENGTH_LONG).show();//测试成功
        }
    }

    private  Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case AppConst.CREATE_TRIP_RECORD_SUCCES:
                    Toast.makeText(mContext,"解锁成功，祝您骑行愉快！",Toast.LENGTH_SHORT).show();

                    scancode_iv.setVisibility(View.INVISIBLE);
                    /**解锁成功后，显示为解锁状态，设置为可点击*/
                    Drawable drawable1 = mResources.getDrawable(R.drawable.lockopen_selector);
                    endTrip.setBackground(drawable1);
                    endTrip.setVisibility(View.VISIBLE);
                    endTrip.setClickable(true);



                    break;
                case AppConst.CREATE_TRIP_RECORD_FAIL:
                    Toast.makeText(mContext,"创建出行记录失败！",Toast.LENGTH_SHORT).show();
                    break;
                case AppConst.UPDATE_TRIP_RECORD_SUCCESS:
                    Toast.makeText(mContext,"已结束骑行！",Toast.LENGTH_SHORT).show();
                    /**结束骑行后，显示为上锁状态，并设置为不可点击*/
                    Drawable drawable2 = mResources.getDrawable(R.drawable.lock_selector);
                    endTrip.setBackground(drawable2);
                    endTrip.setClickable(false);
                    //TODO
                    HttpUtil.timeDiff(mhandler,timeDiffUrl,user.getEmail(),tripRecord_fakeid);
                    disappearTheLock(mhandler);
                    break;
                case AppConst.UPDATE_TRIP_RECORD_FAIL:
                    Toast.makeText(mContext,"更新出行记录失败！",Toast.LENGTH_SHORT).show();
                    break;
                case AppConst.LOCK_DISAPPEAR:
                    endTrip.setVisibility(View.INVISIBLE);
                    scancode_iv.setVisibility(View.VISIBLE);
                    break;
                case AppConst.TIME_DIFF_SUCCESS:
                    spendmoney=(String)msg.obj;
                    Toast.makeText(mContext,"本次骑行时间为"+spendmoney+"分钟",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,DeductionActivity.class);
                    intent.putExtra("spendmoney",spendmoney);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                    break;
                case AppConst.LOCK_THE_BIKE_SUEECSS:
                    Toast.makeText(mContext,"上锁成功，请您慢走！",Toast.LENGTH_SHORT).show();
                    break;
                case AppConst.LOCK_THE_BIKE_FAIL:
                    Toast.makeText(mContext,"上锁失败！",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void disappearTheLock(final Handler mhandler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(3000);
                    mhandler.obtainMessage(AppConst.LOCK_DISAPPEAR).sendToTarget();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
