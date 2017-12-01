package com.tempoup.sdj.humi;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.support.v7.app.ActionBar;

import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapView;



public class MainActivity extends AppCompatActivity{



    CharFragment m_CharFrag;
    NaviFragment m_NaviFrag;
    HealthFragment m_HealthFrag;

    int m_FragIndex = -1;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {



            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            switch (item.getItemId()) {

                case R.id.navigation_Chracter:
                    m_CharFrag.setAniThreadRun(true);
                    fragmentTransaction.replace(R.id.FrameFragment, m_CharFrag);
                    fragmentTransaction.commit();
                    m_FragIndex = 0;
                    return true;
                case R.id.navigation_Navi:
                    m_CharFrag.setAniThreadRun(false);
                    fragmentTransaction.replace(R.id.FrameFragment, m_NaviFrag);
                    fragmentTransaction.commit();
                    m_FragIndex = 1;
                    return true;
                case R.id.navigation_Health:
                    m_CharFrag.setAniThreadRun(false);
                    fragmentTransaction.replace(R.id.FrameFragment, m_HealthFrag);
                    fragmentTransaction.commit();
                    m_FragIndex = 2;
                    return true;

            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        m_CharFrag = new CharFragment();
        m_NaviFrag = new NaviFragment();
        m_HealthFrag = new HealthFragment();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.FrameFragment, m_CharFrag);
        fragmentTransaction.commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //Custom Action Bar
        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //getSupportActionBar().setCustomView(R.layout.custom_bar);

        //mTextMessage = (TextView) findViewById(R.id.message);

        new Thread(){
            public void run(){
                while(true){
                    try{
                        sleep(1000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }

                    Singleton.getInstance().setCurTime(System.currentTimeMillis());
                    if( m_FragIndex == 1 ){
                        m_NaviFrag.getTickCountTimeLeft();
                    }
                }

            }
        }.start();

    }

    @Override
    public void onBackPressed() {
        if ( m_NaviFrag.getTmapView().getVisibility() == View.INVISIBLE ){
            m_NaviFrag.HideListview();
        }
        else{
            super.onBackPressed();
        }

    }

}
