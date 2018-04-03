package com.panghui.dreambike.Util;

import android.util.Log;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.panghui.dreambike.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
        /**利用JSONObject来解析JSON数据*/
        public static User parseJSONWithJSONObject(String jsonData){
            User user=new User();
            try{
                JSONArray jsonArray=new JSONArray(jsonData);
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    user.setUsername(jsonObject.getString("username"));
                    user.setEmail(jsonObject.getString("email"));
                    user.setPassword(jsonObject.getString("password"));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return user;
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
}
