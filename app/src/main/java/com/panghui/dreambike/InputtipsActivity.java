package com.panghui.dreambike;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.panghui.dreambike.Util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InputtipsActivity extends AppCompatActivity implements TextWatcher,Inputtips.InputtipsListener,
        AdapterView.OnItemClickListener,GeocodeSearch.OnGeocodeSearchListener{
    private String city="桂林";
    private AutoCompleteTextView mKeywordText;
    private ListView minputlist;

    private GeocodeSearch geocoderSearch;
    private String addressName;
    private ProgressBar loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputtips);
        minputlist=(ListView)findViewById(R.id.inputlist);
        minputlist.setOnItemClickListener(this);
        mKeywordText=(AutoCompleteTextView)findViewById(R.id.input_edittext);
        mKeywordText.addTextChangedListener(this);

        geocoderSearch=new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        loadingbar=(ProgressBar)findViewById(R.id.loadingbar);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText=s.toString().trim();
        InputtipsQuery inputquery=new InputtipsQuery(newText,city);
        inputquery.setCityLimit(true);
        Inputtips inputTips=new Inputtips(InputtipsActivity.this,inputquery);
        inputTips.setInputtipsListener(this);
        inputTips.requestInputtipsAsyn();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
    /**
     * tipList 返回的结果列表
     * rCode 错误码
     * */
    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        if (rCode== AMapException.CODE_AMAP_SUCCESS){
            List<HashMap<String,String>> listString=new ArrayList<>();/**哈希列表**/
            for (int i=0;i<tipList.size();i++){
                HashMap<String,String> map=new HashMap<>();/**一个哈希键值对，装配好后存入listString*/
                map.put("name",tipList.get(i).getName());
                map.put("address",tipList.get(i).getName());
                listString.add(map);
            }
            SimpleAdapter aAdapter=new SimpleAdapter(getApplicationContext(),listString,R.layout.item_layout,
                    new String[]{"name","address"},new int[]{R.id.poi_field_id,R.id.poi_value_id});
            minputlist.setAdapter(aAdapter);
            aAdapter.notifyDataSetChanged();
        }else{
            ToastUtil.show(this.getApplicationContext(),rCode);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String,String> result=(HashMap<String, String>) minputlist.getItemAtPosition(position);
        //Toast.makeText(this,"The selected item is"+ result.get("name"),Toast.LENGTH_LONG).show();
        loadingbar.setVisibility(View.VISIBLE);
        getLatlon(result.get("name"));
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {

    }
    /**
     * 地理编码查询回调
     * */
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        if (rCode==AMapException.CODE_AMAP_SUCCESS){
            if (result!=null&&result.getGeocodeAddressList()!=null
                    &&result.getGeocodeAddressList().size()>0){
                GeocodeAddress address=result.getGeocodeAddressList().get(0);
                addressName="经纬度值："+address.getLatLonPoint()+"\n位置描述："
                        +address.getFormatAddress();
                loadingbar.setVisibility(View.INVISIBLE);
                //ToastUtil.show(testForError.this,addressName);
                sendToMainActivity(address.getLatLonPoint().toString());
            }else{
                ToastUtil.show(InputtipsActivity.this,"对不起，没有搜索到相关数据！");
            }
        }else{
            ToastUtil.show(this,rCode);
        }

    }
    /**
     * 响应地理编码
     * */
    public void getLatlon(final String name){
        GeocodeQuery query=new GeocodeQuery(name,"桂林");
        geocoderSearch.getFromLocationNameAsyn(query);
    }

    public void sendToMainActivity(String point){
        Intent intent=new Intent();
        intent.putExtra("LatLng",point);
        setResult(RESULT_OK,intent);
        finish();
    }
}
