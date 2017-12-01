package com.tempoup.sdj.humi;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class HealthFragment extends Fragment {
    static final int D_DATETYPE_TODAY = 0;
    static final int D_DATETYPE_WEEK = 1;
    static final int D_DATETYPE_MONTH = 2;

    private int m_nDateType;
    private LinearLayout m_detailDateLayout;
    private TextView m_dateText;
    private Calendar m_cal;
    private TextView m_todayText;
    private TextView m_weekText;
    private TextView m_monthText;

    public HealthFragment() {
        m_detailDateLayout = null;
        m_nDateType = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);

        m_detailDateLayout = view.findViewById(R.id.detailDateLayout);
        m_todayText = view.findViewById(R.id.todayTextView);
        m_weekText = view.findViewById(R.id.weekTextView);
        m_monthText = view.findViewById(R.id.monthTextView);
        m_dateText = view.findViewById(R.id.rangeDateText);

        m_cal = Calendar.getInstance();

        m_todayText.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                m_nDateType = D_DATETYPE_TODAY;
                showDetailDateInLayout(D_DATETYPE_TODAY);
            }
        });
        m_weekText.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                m_nDateType = D_DATETYPE_WEEK;
                showDetailDateInLayout(D_DATETYPE_WEEK);
            }
        });
        m_monthText.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                m_nDateType = D_DATETYPE_MONTH;
                showDetailDateInLayout(D_DATETYPE_MONTH);
            }
        });


        showDetailDateInLayout(D_DATETYPE_TODAY);
        return view;
    }

    //@SuppressLint("SetTextI18n")
    @SuppressLint("ResourceAsColor")
    public void showDetailDateInLayout(int nSelectDateType){
        String []days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String []months = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Agu", "Sep", "Oct", "Nov", "Dev"};
        int weekmin = m_cal.get(Calendar.DAY_OF_MONTH) - 3;
        int weekmax = m_cal.get(Calendar.DAY_OF_MONTH) + 3;
        switch(nSelectDateType){
            case D_DATETYPE_TODAY:

                m_todayText.setTextColor(Color.rgb(230, 0, 0));
                m_weekText.setTextColor(R.color.Default_textColor);
                m_monthText.setTextColor(R.color.Default_textColor);
                m_dateText.setText(

                                days[m_cal.get(Calendar.DAY_OF_WEEK)] +
                                        ", " +
                                String.valueOf(m_cal.get(Calendar.DAY_OF_MONTH)) +
                                        " " +
                                months[m_cal.get(Calendar.MONTH)] +
                                        " " +
                                String.valueOf(m_cal.get(Calendar.YEAR))
                );
//                m_detailDateLayout.addView();
                break;

            case D_DATETYPE_WEEK:
                m_weekText.setTextColor(Color.rgb(230, 0, 0));
                m_todayText.setTextColor(R.color.Default_textColor);
                m_monthText.setTextColor(R.color.Default_textColor);
                m_dateText.setText(

                        days[m_cal.get(Calendar.DAY_OF_WEEK)] +
                                ", " +
                                String.valueOf(weekmin) +
                                " ~ " +
                                String.valueOf(weekmax) +
                                " " +
                                months[m_cal.get(Calendar.MONTH)] +
                                " " +
                                String.valueOf(m_cal.get(Calendar.YEAR))
                );
//                m_detailDateLayout.addView();
                break;

            case D_DATETYPE_MONTH:
                m_monthText.setTextColor(Color.rgb(230, 0, 0));
                m_weekText.setTextColor(R.color.Default_textColor);
                m_todayText.setTextColor(R.color.Default_textColor);
                m_dateText.setText(
                                months[m_cal.get(Calendar.MONTH)] +
                                " " +
                                String.valueOf(m_cal.get(Calendar.YEAR))
                );
//                m_detailDateLayout.addView();
                break;

            default:
                break;
        }
    }
}