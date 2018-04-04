package com.panghui.dreambike;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.panghui.dreambike.Util.HttpUtil;
import com.panghui.dreambike.Util.User;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    /**登录部分*/
    private EditText username;
    private EditText password;
    private Button Login_bt;
    private Button Register_bt;
    private User user;
    /**记住密码*/
    private CheckBox rememberPass;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    /**广播部分*/
    private LocalBroadcastManager localBroadcastManager;
    /**广播接收部分*/
    private LoginReciverFromRegister loginReciverFromRegister;
    private IntentFilter intentFilter;
    /**url*/
    private String LoginUrl="http://120.79.91.50/DreamBike/DreamBike_login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /**登录部分获取实例*/
        username=(EditText)findViewById(R.id.username);
        password=(EditText)findViewById(R.id.password);
        /**记住密码*/
        rememberPass=(CheckBox)findViewById(R.id.remember_pass);
        pref= PreferenceManager.getDefaultSharedPreferences(this);

        Login_bt=(Button)findViewById(R.id.login);
        Register_bt=(Button)findViewById(R.id.regist);
        /**获取广播管理器实例*/
        localBroadcastManager=LocalBroadcastManager.getInstance(this);
        /**广播接收部分*/
        intentFilter=new IntentFilter();
        intentFilter.addAction("com.panghui.dreambike.REGIST");
        loginReciverFromRegister=new LoginReciverFromRegister();
        localBroadcastManager.registerReceiver(loginReciverFromRegister,intentFilter);
        /**为按钮注册*/
        Login_bt.setOnClickListener(this);
        Register_bt.setOnClickListener(this);

        boolean isRemember=pref.getBoolean("remember_password",false);
        if (isRemember){
            String usernameS=pref.getString("username","");
            String passwordS=pref.getString("password","");
            username.setText(usernameS);
            password.setText(passwordS);
            rememberPass.setChecked(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(loginReciverFromRegister);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.login:
                String userString=username.getText().toString().trim();
                String passString=password.getText().toString().trim();
                if (userString.equals("")||passString.equals("")){
                    Toast.makeText(LoginActivity.this,
                            "username or password can't be null!",Toast.LENGTH_LONG).show();
                }else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                OkHttpClient client = new OkHttpClient();
                                RequestBody requestBody = new FormBody.Builder()
                                        .add("user", username.getText().toString())
                                        .add("pass", password.getText().toString())
                                        .build();
                                Request request = new Request.Builder()
                                        .url(LoginUrl)
                                        .post(requestBody)
                                        .build();
                                final Response response = client.newCall(request).execute();
                                if (response.code() == 200) {
                                    String jsondata = response.body().string();
                                    if (jsondata.equals("fail")){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(LoginActivity.this,
                                                        "fail to log in,Please try again!",Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }else {
                                        user = HttpUtil.parseJSONWithJSONObject(jsondata);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent=new Intent("com.panghui.dreambike.LOCAL_BROADCAST");
                                                intent.putExtra("username",user.getUsername());
                                                intent.putExtra("email",user.getEmail());
                                                localBroadcastManager.sendBroadcast(intent);
                                                finish();
                                            }
                                        });
                                    }

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    editor=pref.edit();
                    if (rememberPass.isChecked()){
                        editor.putBoolean("remember_password",true);
                        editor.putString("username",username.getText().toString().trim());
                        editor.putString("password",password.getText().toString().trim());
                    }else {
                        editor.clear();
                    }
                    editor.apply();
                }

                break;
            case R.id.regist:
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                break;
             default:
        }
    }
    /***********************************function define******************************************************************/

    /*****************************class define******************************************************************/
    class LoginReciverFromRegister extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String usernameS=intent.getStringExtra("username");
            String passwordS=intent.getStringExtra("password");
            username.setText(usernameS);
            password.setText(passwordS);
        }
    }


}
