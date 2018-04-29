package com.panghui.dreambike;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.panghui.dreambike.Util.User;

public class WalletActivity extends AppCompatActivity {
    private ImageView wallet;
    private TextView balance;
    private Button chargenow;

    private User user;
    private int myBalance_int;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        chargenow=(Button)findViewById(R.id.chargenow);
        balance=(TextView)findViewById(R.id.balance);
        wallet=(ImageView)findViewById(R.id.wallet);

        Intent intent=getIntent();
        user=(User)intent.getSerializableExtra("user");
        balance.setText(user.getBalance());//设置初始balance;

        /**为图片实现圆角*/
        Resources res=getResources();
        Bitmap src=BitmapFactory.decodeResource(getResources(),R.drawable.wallet1);
        RoundedBitmapDrawable dr=
                RoundedBitmapDrawableFactory.create(res,src);
        dr.setCornerRadius(20L);
        wallet.setImageDrawable(dr);

        /**立即充值按钮*/
        chargenow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(WalletActivity.this,ChargeActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            }
        });
    }


}
