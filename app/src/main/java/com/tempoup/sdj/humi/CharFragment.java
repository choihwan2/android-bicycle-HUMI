package com.tempoup.sdj.humi;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InterruptedIOException;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class CharFragment extends Fragment {
    static final String[] D_LEVEL_NAME = { "농민", "양반", "참판", "영의정", "왕"};
    static final int D_MAX_FRAME = 4;

    private Realm m_Realm;

    private ImageView m_charImgView;
    private ProgressBar m_expBar;
    private TextView m_levelText;
    private TextView m_levelClassText;
    private TextView m_mainTextView;
    private ImageView m_progressIcon;
    private ImageView m_charLeft;
    private ImageView m_charRight;
    private TextView m_userNameText;

    private int m_nLevel;
    private int m_nExp;

    private int m_nCharFrame;
    private boolean m_nAniThreadRun;

    private int m_nLevelPivot;

    private String m_sUserName;

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
                    if( m_nCharFrame <= 0 ) {
                        m_nCharFrame = D_MAX_FRAME;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setChangeCharacter(m_nLevelPivot, m_nCharFrame);
                        }
                    });
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
        m_mainTextView = null;
        m_progressIcon = null;
        m_charLeft = null;
        m_charRight = null;

        m_nExp = 0;
        m_nCharFrame = D_MAX_FRAME;

        m_nAniThreadRun = true;
        m_sUserName = null;
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
        m_mainTextView = (TextView) view.findViewById(R.id.charMainText);
        m_progressIcon = (ImageView) view.findViewById(R.id.progressIcon);
        m_charLeft = (ImageView) view.findViewById(R.id.charLeft);
        m_charRight = (ImageView) view.findViewById(R.id.charRight);
        m_userNameText = (TextView) view.findViewById(R.id.userNameText);

        m_charLeft.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                showLowerLevelChar();
            }
        });
        m_charRight.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                showHigherLevelChar();
            }
        });

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

        m_nLevelPivot = m_nLevel;
        m_nExp = exp;

        setChangeCharacter(m_nLevel, m_nCharFrame);
        setChangeText(String.valueOf(m_nLevel));
        setChangeExp(exp);
        setChangeLevelText(m_nLevel);

        m_sUserName = new String();




        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable()
        {
            @Override     public void run()
            {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();
                if( user != null) {
                    m_sUserName = user.getDisplayName() + m_userNameText.getText().toString();
                    m_userNameText.setText(m_sUserName);
                }
            }
        }, 3000);



        setAniThreadRun(true);


        AnimationThread aniThread = new AnimationThread();
        aniThread.setAnimationDelay(230);
        aniThread.start();

        ApeearMoveAnimate();

        return view;
    }

    public void showLowerLevelChar(){
        if( m_nLevelPivot <= 1 ) { return; }
        m_nLevelPivot--;
        setChangeCharacter(m_nLevelPivot, m_nCharFrame);
        setChangeText(String.valueOf(m_nLevelPivot));
        ApeearMoveAnimate();
    }

    public void showHigherLevelChar(){
        if( m_nLevelPivot >= 5 ) { return; }
        m_nLevelPivot++;
        setChangeCharacter(m_nLevelPivot, m_nCharFrame);
        setChangeText(String.valueOf(m_nLevelPivot));
        ApeearMoveAnimate();
    }

    public void ApeearMoveAnimate(){
        DisplayMetrics displayMetrics = new DisplayMetrics();

        getActivity().getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);

        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        m_charImgView.setTranslationX(width);
        m_charImgView.animate().translationX(0).setDuration(2000);
    }

    public void setChangeExp(int exp)
    {
        m_expBar.post(new Runnable() {
            @Override
            public void run() {
                DisplayMetrics displayMetrics = new DisplayMetrics();

                getActivity().getWindowManager()
                        .getDefaultDisplay()
                        .getMetrics(displayMetrics);

                int height = displayMetrics.heightPixels;
                float width = displayMetrics.widthPixels;


                m_expBar.setProgress(m_nExp);
                float fExpfloat = (float)m_nExp / 100;
                m_progressIcon.setTranslationX(width * fExpfloat);
            }
        });
    }

    public void setChangeText(String lvStr)
    {
        m_levelText.setText("Lv " + lvStr);
        m_levelClassText.setText(D_LEVEL_NAME[Integer.parseInt(lvStr)-1]);

    }

    public void setChangeLevelText(int level){
        Resources res = getActivity().getResources();

        String str = res.getString(R.string.lv1_Intro);
        switch(level){
            case 1:
                m_mainTextView.setText(res.getString(R.string.lv1_Intro));
                break;

            case 2:
                m_mainTextView.setText(res.getString(R.string.lv2_Intro));
                break;

            case 3:
                m_mainTextView.setText(res.getString(R.string.lv3_Intro));
                break;

            case 4:
                m_mainTextView.setText(res.getString(R.string.lv4_Intro));
                break;

            case 5:
                m_mainTextView.setText(res.getString(R.string.lv5_Intro));
                break;

            default:
                break;
        }
    }


    public void setChangeCharacter(int level, int frame)
    {
        Resources res = getActivity().getResources();
        int id = res.getIdentifier("lv" + String.valueOf(level) + "_" + String.valueOf(frame), "mipmap", getActivity().getPackageName());


        ColorMatrix matrix = new ColorMatrix();

        if( m_nLevel < level ){
            // 흑백 처리
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            m_charImgView.setColorFilter(filter);
        }else{
            m_charImgView.setColorFilter(null);
        }





        m_charImgView.setImageResource(id);

        //id = res.getIdentifier("lv" + String.valueOf(level) + "_" + String.valueOf(frame) + "_pgicon", "mipmap", getActivity().getPackageName());
        //m_progressIcon.setImageResource(id);
    }
}