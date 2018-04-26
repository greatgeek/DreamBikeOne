package com.panghui.dreambike;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.panghui.dreambike.Util.HttpUtil;
import com.panghui.dreambike.Util.TripRecordItem;
import com.panghui.dreambike.dialog.MyLoadDialog;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TripRecord extends AppCompatActivity {
    private String tripRecordjsonData;
    private List<TripRecordItem> tripRecordList;
    List<String> data=new ArrayList<>();
    ListView tripRecord_lv;
    private String email;

    final String tripRecordUrl="http://120.79.91.50/DreamBike/DreamBike_triprecord.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_record);
        tripRecord_lv=(ListView)findViewById(R.id.tripRecord_lv);

        Intent intent=getIntent();
        email=intent.getStringExtra("email");

        new getTripRecord().execute(tripRecordUrl,email);
        showMyDialog();

        tripRecord_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String result=data.get(position);
                Intent intent=new Intent(TripRecord.this,MyTripActivity.class);
                for (TripRecordItem item:tripRecordList){
                    if (item.getCreatetime()==result){
                        intent.putExtra("user_slatitude",item.getUser_slatitude());
                        intent.putExtra("user_slongtitude",item.getUser_slongtitude());
                        intent.putExtra("user_dlatitude",item.getUser_dlatitude());
                        intent.putExtra("user_dlongtitude",item.getUser_dlongtitude());
                        break;
                    }
                }
                startActivity(intent);
            }
        });
    }

    /**显示加载框*/
    private void showMyDialog(){
        MyLoadDialog myLoadDialog=MyLoadDialog.getInstance();
        myLoadDialog.setStyle(MyLoadDialog.STYLE_NORMAL,R.style.load_dialog);
        MyLoadDialog.getInstance().show(getSupportFragmentManager(),"");
    }

    public class getTripRecord extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            String jsonData = null;
            try{
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("email",strings[1])
                        .build();
                Request request = new Request.Builder()
                        .url(strings[0])
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                if (response.code()==200){
                    jsonData=response.body().string();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return jsonData;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            if (jsonData!=null){
                tripRecordjsonData=jsonData;
                tripRecordList= HttpUtil.itemparseJSONWithJSONObjec(tripRecordjsonData);
                for (TripRecordItem item:tripRecordList){
                    data.add(item.getCreatetime());
                }
                ArrayAdapter<String> adapter=new ArrayAdapter<>(TripRecord.this,android.R.layout.simple_list_item_1,data);
                tripRecord_lv.setAdapter(adapter);

            }else{
                Toast.makeText(TripRecord.this,"获取记录失败！",Toast.LENGTH_SHORT).show();
            }

            MyLoadDialog.getInstance().dismiss();

        }
    }
}
