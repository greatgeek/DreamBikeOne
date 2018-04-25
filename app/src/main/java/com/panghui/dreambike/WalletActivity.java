package com.panghui.dreambike;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class WalletActivity extends AppCompatActivity {
    private ImageView wallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        /**为图片实现圆角*/
        wallet=(ImageView)findViewById(R.id.wallet);
        Resources res=getResources();
        Bitmap src=BitmapFactory.decodeResource(getResources(),R.drawable.wallet1);
        RoundedBitmapDrawable dr=
                RoundedBitmapDrawableFactory.create(res,src);
        dr.setCornerRadius(20L);
        wallet.setImageDrawable(dr);
    }


}
