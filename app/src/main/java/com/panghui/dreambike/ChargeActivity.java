package com.panghui.dreambike;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.panghui.dreambike.Util.HttpUtil;
import com.panghui.dreambike.Util.User;
import com.panghui.dreambike.base.AppConst;

public class ChargeActivity extends AppCompatActivity {
    private EditText chargeblance;
    private EditText paypassword;
    private Button confirm;

    private User user;
    private String chargeUrl="http://120.79.91.50/DreamBike/DreamBike_charge.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge);

        chargeblance=(EditText)findViewById(R.id.chargeblance);
        paypassword=(EditText)findViewById(R.id.paypassword);
        confirm=(Button) findViewById(R.id.confirm);

        user=User.getInstance();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chargeblance.getText().toString().equals("")||
                        paypassword.getText().toString().equals("")){
                    Toast.makeText(ChargeActivity.this, "金额或密码不能为空!", Toast.LENGTH_SHORT).show();
                }else if(user.getPassword().equals(paypassword.getText().toString())){
                    try {
                        int balanceInt = Integer.parseInt(user.getBalance());
                        int chargeInput=Integer.parseInt(chargeblance.getText().toString());
                        int sum=balanceInt+chargeInput;
                        String balance=new Integer(sum).toString();
                        user.setBalance(balance);
                        HttpUtil.charge(mhandler,chargeUrl,user.getEmail(),user.getBalance());

                    }catch (NumberFormatException e){
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(ChargeActivity.this,"密码输入错误,请重新输入！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case AppConst.RECHARGE_SUCCESS:
                    Toast.makeText(ChargeActivity.this,"充值成功！",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case AppConst.RECHARGE_FAIL:
                    Toast.makeText(ChargeActivity.this,"充值失败,请重试！",Toast.LENGTH_SHORT).show();
                    break;
                default:

            }
        }
    };
}
