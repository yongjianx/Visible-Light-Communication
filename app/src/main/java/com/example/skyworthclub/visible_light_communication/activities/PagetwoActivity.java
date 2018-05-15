package com.example.skyworthclub.visible_light_communication.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.skyworthclub.visible_light_communication.activities.ControllerActivity;
import com.example.skyworthclub.visible_light_communication.R;

public class PagetwoActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView tabHome;
    private TextView tabSell;
    private TextView tabFollow;
    private TextView tabUser;
    private TextView tabToilet;
    private TextView tabElevator;
    private TextView tabElevator2;
    private TextView tabExit;
    private TextView tabSign;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wl_activity_pagetwo);
        bindView();
    }

    //UI组件初始化与事件绑定
    private void bindView() {
        tabHome = (TextView)this.findViewById(R.id.txt_home);
        tabSell = (TextView)this.findViewById(R.id.txt_sell);
        tabFollow = (TextView)this.findViewById(R.id.txt_follow);
        tabUser = (TextView)this.findViewById(R.id.txt_user);
        tabToilet = (TextView)this.findViewById(R.id.txt_toilet);
        tabElevator = (TextView)this.findViewById(R.id.txt_elevator);
        tabElevator2 = (TextView)this.findViewById(R.id.txt_elevator2);
        tabExit = (TextView)this.findViewById(R.id.txt_exit);
        tabSign = (TextView)this.findViewById(R.id.txt_sign);

        tabHome.setOnClickListener(this);
        tabSell.setOnClickListener(this);
        tabFollow.setOnClickListener(this);
        tabUser.setOnClickListener(this);
        tabToilet.setOnClickListener(this);
        tabElevator.setOnClickListener(this);
        tabElevator2.setOnClickListener(this);
        tabExit.setOnClickListener(this);
        tabSign.setOnClickListener(this);
    }

    //重置所有文本的选中状态
    public void selected(){
        tabHome.setSelected(false);
        tabSell.setSelected(false);
        tabFollow.setSelected(false);
        tabUser.setSelected(false);
        tabToilet.setSelected(false);
        tabElevator.setSelected(false);
        tabElevator2.setSelected(false);
        tabExit.setSelected(false);
        tabSign.setSelected(false);
    }

    public void selected_follow(){
        tabHome.setSelected(false);
        tabSell.setSelected(false);
        tabFollow.setSelected(false);
        tabUser.setSelected(false);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.txt_home:
                selected();
                tabHome.setSelected(true);
                Intent intentMain = new Intent(PagetwoActivity.this, MainActivity.class);
                intentMain.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentMain);
                finish();
                break;

            case R.id.txt_sell:
                selected();
                tabSell.setSelected(true);
                Intent intent = new Intent(PagetwoActivity.this,SaleActivity.class);
                startActivity(intent);
                break;

            case R.id.txt_follow:
                selected_follow();
                tabFollow.setSelected(true);
                Intent intent_ = new Intent(PagetwoActivity.this, ControllerActivity.class);
                intent_.putExtra("key_number", 3);
                startActivity(intent_);
                break;

            case R.id.txt_user:
                selected();
                tabUser.setSelected(true);
                break;

            case R.id.txt_toilet:
                selected();
                tabToilet.setSelected(true);
                break;

            case R.id.txt_elevator:
                selected();
                tabElevator.setSelected(true);
                break;

            case R.id.txt_elevator2:
                selected();
                tabElevator2.setSelected(true);
                break;

            case R.id.txt_exit:
                selected();
                tabExit.setSelected(true);
                break;

            case R.id.txt_sign:
                selected();
                tabSign.setSelected(true);

        }
    }
}
