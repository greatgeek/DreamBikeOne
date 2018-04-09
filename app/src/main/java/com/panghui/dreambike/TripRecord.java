package com.panghui.dreambike;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.panghui.dreambike.Util.HttpUtil;
import com.panghui.dreambike.Util.TripRecordItem;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class TripRecord extends AppCompatActivity {
    private String tripRecordjsonData;
    private List<TripRecordItem> tripRecordList;
    List<String> data=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_record);
        ListView tripRecord_lv=(ListView)findViewById(R.id.tripRecord_lv);


        Intent intent=getIntent();
        tripRecordjsonData=intent.getStringExtra("tripRecordjsonData");
        tripRecordList= HttpUtil.itemparseJSONWithJSONObjec(tripRecordjsonData);
        for (TripRecordItem item:tripRecordList){
            data.add(item.getCreatetime());
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<>(TripRecord.this,android.R.layout.simple_list_item_1,data);
        tripRecord_lv.setAdapter(adapter);
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
}
