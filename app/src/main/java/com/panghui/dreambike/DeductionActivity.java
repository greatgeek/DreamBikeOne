package com.panghui.dreambike;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.panghui.dreambike.Util.HttpUtil;
import com.panghui.dreambike.Util.User;
import com.panghui.dreambike.base.AppConst;

import org.w3c.dom.Text;

import java.text.DecimalFormat;

public class DeductionActivity extends AppCompatActivity {

    private User user;
    private DecimalFormat df = new DecimalFormat("0.00");//规范浮点型数据的格式
    private double current_balance;//当前金额
    private String spendmoney;
    private double spendmoney_double;//花费
    private double leftover;//扣费后金额

    private final double UNIT_PRICE=0.1;

    private ImageView deduction_im;
    private TextView current_amount;
    private TextView deduction;
    private TextView amount_after_deduction;
    private Button confirm;

    private String chargeUrl="http://120.79.91.50/DreamBike/DreamBike_charge.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deduction);

        Intent intent=getIntent();
        spendmoney=intent.getStringExtra("spendmoney");
        spendmoney_double=Double.parseDouble(spendmoney);
        user = User.getInstance();
        current_balance=Double.parseDouble(user.getBalance());
        leftover=current_balance-spendmoney_double*UNIT_PRICE;//每骑行一分钟，收费一角

        deduction_im=(ImageView)findViewById(R.id.deduction_im);
        current_amount=(TextView)findViewById(R.id.current_amount);
        deduction=(TextView)findViewById(R.id.deduction);
        amount_after_deduction=(TextView)findViewById(R.id.amount_after_deduction);
        confirm=(Button)findViewById(R.id.confirm);

        /**为图片实现圆角化*/
        Resources res=getResources();
        Bitmap src= BitmapFactory.decodeResource(res,R.drawable.wallet_deduction);
        RoundedBitmapDrawable dr=
                RoundedBitmapDrawableFactory.create(res,src);
        dr.setCornerRadius(20L);
        deduction_im.setImageDrawable(dr);

        current_amount.setText(user.getBalance());
        deduction.setText(df.format(spendmoney_double*UNIT_PRICE));

        amount_after_deduction.setText(df.format(leftover));

        HttpUtil.deduct(mhandler,chargeUrl,user.getEmail(),df.format(leftover));

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    public Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case AppConst.DEDUCTION_SUCCESS:
                    Toast.makeText(DeductionActivity.this,"结算成功!",Toast.LENGTH_SHORT).show();
                    break;
                case AppConst.DEDUCTION_FAIL:
                    Toast.makeText(DeductionActivity.this,"结算失败！",Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
        }
    };
}
