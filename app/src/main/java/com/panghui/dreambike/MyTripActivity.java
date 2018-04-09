package com.panghui.dreambike;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.panghui.dreambike.Util.AMapUtil;
import com.panghui.dreambike.Util.ToastUtil;
import com.panghui.dreambike.dialog.LoadDialog;
import com.panghui.dreambike.overlay.RideRouteOverlay;

public class MyTripActivity extends AppCompatActivity implements RouteSearch.OnRouteSearchListener{
    private String user_slatitude;
    private String user_slongtitude;
    private String user_dlatitude;
    private String user_dlongtitude;
    /***地图*/
    private AMap aMap;
    private MapView mapView=null;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private RideRouteResult mRideRouteResult;
    private LatLonPoint mStartPoint;
    private LatLonPoint mEndPoint;
    private final int ROUTE_TYPE_RIDE=4;
    /**时间距离*/
    private TextView time;
    private TextView distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trip);
        mContext=this.getApplicationContext();
        /**获取地图控件引用**/
        mapView=(MapView)findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        /**时间和距离TextView*/
        time=(TextView)findViewById(R.id.time);
        distance=(TextView)findViewById(R.id.distance);
        /**获取Intent对象**/
        Intent intent =getIntent();
        user_slatitude=intent.getStringExtra("user_slatitude");
        user_slongtitude=intent.getStringExtra("user_slongtitude");
        user_dlatitude=intent.getStringExtra("user_dlatitude");
        user_dlongtitude=intent.getStringExtra("user_dlongtitude");

        mStartPoint=new LatLonPoint(Double.parseDouble(user_slatitude),Double.parseDouble(user_slongtitude));
        mEndPoint=new LatLonPoint(Double.parseDouble(user_dlatitude),Double.parseDouble(user_dlongtitude));

        init();
        setfromandtoMarker();
        searchRouteResult(ROUTE_TYPE_RIDE,RouteSearch.RidingDefault);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult result, int errorCode) {
        LoadDialog.getInstance().dismiss();
        aMap.clear();
        if (errorCode== AMapException.CODE_AMAP_SUCCESS){
            if (result!=null&&result.getPaths()!=null){
                if (result.getPaths().size()>0){
                    mRideRouteResult=result;
                    final RidePath ridePath=mRideRouteResult.getPaths().get(0);
                    RideRouteOverlay rideRouteOverlay=new RideRouteOverlay(
                            this,aMap,ridePath,
                            mRideRouteResult.getStartPos(),
                            mRideRouteResult.getTargetPos()
                    );
                    rideRouteOverlay.removeFromMap();
                    rideRouteOverlay.addToMap();
                    rideRouteOverlay.zoomToSpan();
                    int dis=(int)ridePath.getDistance();
                    int dur=(int)ridePath.getDuration();
                    String disS=new Integer(dis).toString();
                    String durS=new Integer(dur/60).toString();
                    time.setText(durS);
                    distance.setText(disS);
                }else if (result!=null&&result.getPaths()==null){
                    ToastUtil.show(mContext,"对不起，没有搜索到相关数据！");
                }
            }else {
                ToastUtil.show(mContext,"对不起，没有搜索到相关数据！");
            }
        }else {
            ToastUtil.showerror(this.getApplicationContext(),errorCode);
        }
    }
    /**初始化AMap对象**/
    private void init(){
        if (aMap==null){
            aMap=mapView.getMap();
            aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
            CameraUpdate update=CameraUpdateFactory.changeLatLng(new LatLng(mStartPoint.getLatitude(),mStartPoint.getLongitude()));
            aMap.animateCamera(update);
            aMap.getUiSettings().setZoomControlsEnabled(false);
        }
        mRouteSearch=new RouteSearch(this);
        registerListener();
    }
    /**注册监听**/
    private void registerListener(){
        mRouteSearch.setRouteSearchListener(this);
    }
    private void showDialog(){
        LoadDialog loadDialog=LoadDialog.getInstance();
        loadDialog.setStyle(DialogFragment.STYLE_NORMAL,R.style.load_dialog);
        LoadDialog.getInstance().show(getSupportFragmentManager(),"");
    }

    private void setfromandtoMarker(){
        aMap.addMarker(new MarkerOptions()
        .position(AMapUtil.convertToLatLng(mStartPoint))
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_start)));
        aMap.addMarker(new MarkerOptions()
        .position(AMapUtil.convertToLatLng(mEndPoint))
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_end)));
    }

    /**开始搜索路径规划方案**/
    public void searchRouteResult(int routeType,int mode){
        if (mStartPoint==null){
            ToastUtil.show(mContext,"定位中，稍后再试...");
            return;
        }
        if (mEndPoint==null){
            ToastUtil.show(mContext,"终点未设置");
        }
        showDialog();
        final RouteSearch.FromAndTo fromAndTo=new RouteSearch.FromAndTo(mStartPoint,mEndPoint);
        if (routeType==ROUTE_TYPE_RIDE){
            RouteSearch.RideRouteQuery query=new RouteSearch.RideRouteQuery(fromAndTo,mode);
            mRouteSearch.calculateRideRouteAsyn(query);
        }
    }
}
