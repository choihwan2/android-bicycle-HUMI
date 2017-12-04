package com.tempoup.sdj.humi;

import android.content.Context;

import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapView;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by sdj on 2017. 11. 19..
 */

public class Singleton {
    private static  Singleton uniqueSingleton;

    private long m_startTime;
    private long m_curTime;
    private float m_totalSpeed;
    private float m_avgSpeed;
    private Realm m_Realm;

    private Singleton() {
        m_startTime = System.currentTimeMillis();
        m_curTime = System.currentTimeMillis();
        m_totalSpeed = 0.0f;
        m_avgSpeed = 0.0f;
    }


    public static Singleton getInstance(){
        if( uniqueSingleton == null ){
            uniqueSingleton = new Singleton();
        }
        return uniqueSingleton;
    }

    public void setInitialize(long millis){
        setStartTime(millis);
        m_totalSpeed = 0.0f;
        m_avgSpeed = 0.0f;
    }

    public void openRealm(Context context){
        Realm.init(context);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("humi")
                .schemaVersion(1)
                .build();

        m_Realm = Realm.getInstance(config);
    }

    public Realm getRealm(){
        return m_Realm;
    }

    public void setCurTime(long millis){
        m_curTime = millis;
    }

    public void setStartTime(long millis){
        m_startTime = millis;
    }

    public int getHour() {
        int time  = (int)(m_curTime - m_startTime) / 1000;

        return (time / 60 % 60);

    }

    public int getMin() {
        int time  = (int)(m_curTime - m_startTime) / 1000;

        return (time % 60);
    }

    public void addTotalSpeed(float speed) {
        m_totalSpeed += speed;
    }

    public float getavgpeed() {
        float totalTime = getHour() * 60 + getMin();

        m_avgSpeed = m_totalSpeed / totalTime;
        return m_avgSpeed;
    }
}
