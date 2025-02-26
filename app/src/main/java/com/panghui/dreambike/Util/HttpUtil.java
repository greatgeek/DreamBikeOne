package com.panghui.dreambike.Util;

import android.os.Handler;
import android.util.Log;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.panghui.dreambike.R;
import com.panghui.dreambike.TripRecord;
import com.panghui.dreambike.base.AppConst;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RunnableFuture;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
        /**利用JSONObject来解析JSON数据*/
        public static void parseJSONWithJSONObject(String jsonData){
            User user=User.getInstance();
            try{
                JSONArray jsonArray=new JSONArray(jsonData);
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    user.setUsername(jsonObject.getString("username"));
                    user.setEmail(jsonObject.getString("email"));
                    user.setPassword(jsonObject.getString("password"));
                    user.setBalance(jsonObject.getString("balance"));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public static List<TripRecordItem> itemparseJSONWithJSONObjec(String jsonData){
            List<TripRecordItem> itemList=new ArrayList<>();
            try{
                JSONArray jsonArray=new JSONArray(jsonData);
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    TripRecordItem item=new TripRecordItem();
                    item.setEmail(jsonObject.getString("email"));
                    item.setUser_slatitude(jsonObject.getString("user_slatitude"));
                    item.setUser_slongtitude(jsonObject.getString("user_slongtitude"));
                    item.setUser_dlatitude(jsonObject.getString("user_dlatitude"));
                    item.setUser_dlongtitude(jsonObject.getString("user_dlongtitude"));
                    item.setCreatetime(jsonObject.getString("createtime"));
                    itemList.add(item);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return itemList;
        }

        public static ArrayList<MarkerOptions> MarkerparseJSONWithJSONObject(String jsonData){//解析JSON数据并返回一个List<MarkerOptions>
            ArrayList<MarkerOptions> markerOptions=new ArrayList<MarkerOptions>();
            BitmapDescriptor bda= BitmapDescriptorFactory//素材a
                    .fromResource(R.drawable.bda);
            BitmapDescriptor bdb=BitmapDescriptorFactory//素材b
                    .fromResource(R.drawable.bdb);
            ArrayList<BitmapDescriptor> giflist=new ArrayList<BitmapDescriptor>();//将动态图素材加载进来
            giflist.add(bda);
            giflist.add(bdb);
            try{
                BitmapDescriptor icon=BitmapDescriptorFactory
                        .fromResource(R.drawable.mark);
                JSONArray jsonArray=new JSONArray(jsonData);
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    String username=jsonObject.getString("username");
                    String latitude=jsonObject.getString("latitude");
                    String longtitude=jsonObject.getString("longtitude");

                    double lat=Double.parseDouble(latitude);
                    double lng=Double.parseDouble(longtitude);
                    LatLng point=new LatLng(lat,lng);
                    MarkerOptions option=new MarkerOptions()
                            .position(point)
                            .icons(giflist)//icons表示多张图片合成的动态图gif，若是要静态图，将其改成icon,再换一张静态图png即可
                            .zIndex(0)
                            .period(40);
                    markerOptions.add(option);

                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return markerOptions;
        }

        //TODO
        public static void createTripRecord(final Handler mhandler,final String url,final int fake_id,final String email,
                                            final String user_slatitude,final String user_slongtitude){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("fake_id",new Integer(fake_id).toString())
                                .add("email",email)
                                .add("user_slatitude",user_slatitude)
                                .add("user_slongtitude",user_slongtitude)
                                .build();
                        Request request = new Request.Builder()
                                .url(url)
                                .post(requestBody)
                                .build();
                        Response response = client.newCall(request).execute();
                        if (response.code()==200){
                            Log.d("createTripRecord()","插入数据成功!");
                            mhandler.obtainMessage(AppConst.CREATE_TRIP_RECORD_SUCCES).sendToTarget();
                        }else{
                            mhandler.obtainMessage(AppConst.CREATE_TRIP_RECORD_FAIL).sendToTarget();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();

        }

        public static void updateTripRecord(final Handler mhandler, final String url, final int fake_id,final String email,
                                            final String user_dlatitude, final String user_dlongtitude){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("fake_id",new Integer(fake_id).toString())
                                .add("email",email)
                                .add("user_dlatitude",user_dlatitude)
                                .add("user_dlongtitude",user_dlongtitude)
                                .build();
                        Request request = new Request.Builder()
                                .url(url)
                                .post(requestBody)
                                .build();
                        Response response = client.newCall(request).execute();
                        if (response.code()==200){
                            Log.d("updateTripRecord()","更新数据成功!");
                            mhandler.obtainMessage(AppConst.UPDATE_TRIP_RECORD_SUCCESS).sendToTarget();
                        }else{
                            mhandler.obtainMessage(AppConst.UPDATE_TRIP_RECORD_FAIL).sendToTarget();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        public static void charge(final Handler mhandler,final String url,final String email,final String balance){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("email",email)
                                .add("balance",balance)
                                .build();
                        Request request = new Request.Builder()
                                .url(url)
                                .post(requestBody)
                                .build();
                        Response response = client.newCall(request).execute();
                        if (response.code()==200){
                            String data=response.body().string().trim();
                            if (data.equals("megneto")){
                                mhandler.obtainMessage(AppConst.RECHARGE_SUCCESS).sendToTarget();
                            }else{
                                mhandler.obtainMessage(AppConst.RECHARGE_FAIL).sendToTarget();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    public static void deduct(final Handler mhandler,final String url,final String email,final String balance){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("email",email)
                            .add("balance",balance)
                            .build();
                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.code()==200){
                        String data=response.body().string().trim();
                        if (data.equals("megneto")){
                            mhandler.obtainMessage(AppConst.DEDUCTION_SUCCESS).sendToTarget();
                        }else{
                            mhandler.obtainMessage(AppConst.DEDUCTION_FAIL).sendToTarget();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

        public static void timeDiff(final Handler mhandler,final String url,final String email,final int fake_id ){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("email", email)
                                .add("fake_id",new Integer(fake_id).toString())
                                .build();
                        Request request = new Request.Builder()
                                .url(url)
                                .post(requestBody)
                                .build();
                        Response response = client.newCall(request).execute();
                        if (response.code()==200){
                            String timevalue=response.body().string().trim();
                            mhandler.obtainMessage(AppConst.TIME_DIFF_SUCCESS,timevalue).sendToTarget();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        public static void lockTheBike(final Handler mhandler,final String url,final String bikeID){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("bikeID",bikeID)
                                .build();
                        Request request = new Request.Builder()
                                .url(url)
                                .post(requestBody)
                                .build();
                        Response response = client.newCall(request).execute();
                        if (response.code()==200){
                            String result = response.body().string();
                            if (result.trim().equals("megneto")){
                                //TODO
                                mhandler.obtainMessage(AppConst.LOCK_THE_BIKE_SUEECSS).sendToTarget();
                            }else{
                                mhandler.obtainMessage(AppConst.LOCK_THE_BIKE_FAIL).sendToTarget();
                            }
                        }else{
                            mhandler.obtainMessage(AppConst.LOCK_THE_BIKE_FAIL).sendToTarget();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();

        }

}
