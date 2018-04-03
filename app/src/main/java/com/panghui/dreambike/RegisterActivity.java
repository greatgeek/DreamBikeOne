package com.panghui.dreambike;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.panghui.dreambike.Util.User;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    /**注册部分*/
    private EditText Email_et;
    private EditText Username_et;
    private EditText Password_et;

    private Button regist_bt;
    private String RegistUrl="http://120.79.91.50/DreamBike/DreamBike_register.php";
    /**广播部分*/
    private LocalBroadcastManager localBroadcastManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        /**广播部分*/
        localBroadcastManager=LocalBroadcastManager.getInstance(this);
        /**注册部分*/
        Email_et=(EditText)findViewById(R.id.email);
        Username_et=(EditText)findViewById(R.id.username);
        Password_et=(EditText)findViewById(R.id.password);
        regist_bt=(Button)findViewById(R.id.regist);
        regist_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email= Email_et.getText().toString().trim();
                final String username=Username_et.getText().toString().trim();
                final String password=Password_et.getText().toString().trim();
                if (email.equals("")||username.equals("")||password.equals("")){
                    Toast.makeText(RegisterActivity.this,"Incomplete information!",Toast.LENGTH_SHORT).show();
                }else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                OkHttpClient client=new OkHttpClient();
                                RequestBody requestBody=new FormBody.Builder()
                                        .add("user",username)
                                        .add("email",email)
                                        .add("pass",password)
                                        .build();
                                Request request=new Request.Builder()
                                        .url(RegistUrl)
                                        .post(requestBody)
                                        .build();
                                final Response response=client.newCall(request).execute();
                                if (response.code()==200){
                                    final String data=response.body().string().trim();
                                    Log.d("RegisterActivity",data);
                                    if (data.equals("success")){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(RegisterActivity.this,
                                                        "Regist successfully!",Toast.LENGTH_SHORT).show();
                                                Intent intent=new Intent("com.panghui.dreambike.REGIST");
                                                intent.putExtra("username",username);
                                                intent.putExtra("password",password);
                                                localBroadcastManager.sendBroadcast(intent);
                                                finish();
                                            }
                                        });
                                    }else{
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(RegisterActivity.this,
                                                        "fail to registe,this email is being used!",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
    }
}
