package com.youle.vu.demo;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {

    private UpdateManager mUpdateManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //检查版本是否需要更新
        mUpdateManager=new UpdateManager(this);
        mUpdateManager.checkUpdateInfo();

    }


}
