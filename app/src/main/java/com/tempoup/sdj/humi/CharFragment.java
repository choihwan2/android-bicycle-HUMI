package com.tempoup.sdj.humi;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InterruptedIOException;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class CharFragment extends Fragment {
    static final String[] D_LEVEL_NAME = { "농민", "양반", "참판", "영의정", "왕"};

    private Realm m_Realm;

    private ImageView m_charImgView;
    private ProgressBar m_expBar;
    private TextView m_levelText;
    private TextView m_levelClassText;

    private int m_nLevel;
    private int m_nExp;

    private int m_nCharFrame;
    private boolean m_nAniThreadRun;

    private class AnimationThread extends Thread {
        private long m_nAnimationDelay;
        private long prevTime;
        private long m_nAdd;
        public AnimationThread()
        { // 초기화 작업//
            m_nAnimationDelay = 500;

            prevTime = System.currentTimeMillis();

        }
        public void setAnimationDelay(long delay){
            m_nAnimationDelay = delay;
        }
        public void run()
        { // 스레드에게 수행시킬 동작들 구현

            while(m_nAniThreadRun){
                //sleep(100);
                long curTime = System.currentTimeMillis();
                if( curTime - prevTime > m_nAnimationDelay ){

                    m_nCharFrame--;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setChangeCharacter(m_nLevel, m_nCharFrame);
                        }
                    });


                    if( m_nCharFrame <= 1 ) {
                        m_nCharFrame = 4;
                    }
                    prevTime = System.currentTimeMillis();
                }

            }
        }
    }


    public CharFragment() {
        // Required empty public constructor
        m_Realm = null;
        m_charImgView = null;
        m_expBar = null;
        m_levelText = null;
        m_levelClassText = null;

        m_nExp = 0;
        m_nCharFrame = 4;

        m_nAniThreadRun = true;
    }

    public void setAniThreadRun(boolean run){
        m_nAniThreadRun = run;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_char, container, false);

        m_charImgView = (ImageView) view.findViewById(R.id.CharImgView);
        m_expBar = (ProgressBar) view.findViewById(R.id.charProgress);
        m_levelText = (TextView) view.findViewById(R.id.LevelText);
        m_levelClassText = (TextView) view.findViewById(R.id.LevelClassText);

        Singleton.getInstance().openRealm(getActivity());
        m_Realm = Singleton.getInstance().getRealm();

        RealmQuery<User> query = m_Realm.where(User.class);

        RealmResults<User> result1 = query.findAll();


        int exp;

        if(result1.size() == 0 ){
            m_Realm.beginTransaction();
            User user = m_Realm.createObject(User.class, 0); // 새 객체 만들기
            user.setLevel(1);
            user.setExp(0);
            //realm.deleteAll();
            m_Realm.commitTransaction();

            m_nLevel = user.getLevel();
            exp = user.getExp();
        }
        else{
            User user = result1.first();
            m_nLevel = user.getLevel();
            exp = user.getExp();
        }

        //초기화 코드
        /*
        m_Realm.beginTransaction();
        User user = result1.first();
        user.setLevel(1);
        user.setExp(0);
        m_nLevel = 1;
        exp = 0;
        m_Realm.commitTransaction();
        */

        m_nExp = exp;

        setChangeCharacter(m_nLevel, m_nCharFrame);
        setChangeText(String.valueOf(m_nLevel));
        setChangeExp(exp);

        // 같은 일들을 한 번에 합니다 ("Fluent interface"):
//        RealmResults<User> result2 = realm.where(User.class)
//                .equalTo("name", "John")
//                .or()
//                .equalTo("name", "Peter")
//                .findAll();


        setAniThreadRun(true);


        AnimationThread aniThread = new AnimationThread();
        aniThread.setAnimationDelay(250);
        aniThread.start();

        return view;
    }

    public void setChangeExp(int exp)
    {
        m_expBar.post(new Runnable() {
            @Override
            public void run() {
                m_expBar.setProgress(m_nExp);
            }
        });
    }

    public void setChangeText(String lvStr)
    {
        m_levelText.setText("Lv " + lvStr);
        m_levelClassText.setText(D_LEVEL_NAME[Integer.parseInt(lvStr)-1]);
    }


    public void setChangeCharacter(int level, int frame)
    {
        Resources res = getActivity().getResources();


        int id = res.getIdentifier("lv" + String.valueOf(level) + "_" + String.valueOf(frame), "mipmap", getActivity().getPackageName());

        m_charImgView.setImageResource(id);
    }
}