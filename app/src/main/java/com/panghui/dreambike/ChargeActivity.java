package com.panghui.dreambike;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.panghui.dreambike.Util.User;

public class ChargeActivity extends AppCompatActivity {
    private EditText chargeblance;
    private EditText paypassword;
    private Button confirm;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge);

        chargeblance=(EditText)findViewById(R.id.chargeblance);
        paypassword=(EditText)findViewById(R.id.paypassword);
        confirm=(Button) findViewById(R.id.confirm);

        Intent intent = getIntent();
        user=(User)intent.getSerializableExtra("user");

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chargeblance.getText().toString().equals("")||
                        paypassword.getText().toString().equals("")){
                    Toast.makeText(ChargeActivity.this, "金额或密码不能为空!", Toast.LENGTH_SHORT).show();
                }else{
                    user.setBalance(chargeblance.getText().toString());
                }
            }
        });
    }
}
