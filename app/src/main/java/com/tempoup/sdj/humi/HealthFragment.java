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

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.LineGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraph;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraphVO;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


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

    private ViewGroup m_layoutGraphView;

    public HealthFragment() {
        m_detailDateLayout = null;
        m_dateText = null;
        m_cal = null;
        m_todayText = null;
        m_weekText = null;
        m_monthText = null;
        m_nDateType = 0;

        m_layoutGraphView = null;
    }



    private void setLineGraph() {
        //all setting
        LineGraphVO vo = makeLineGraphAllSetting();

        //default setting
//		LineGraphVO vo = makeLineGraphDefaultSetting();

        m_layoutGraphView.addView(new LineGraphView(getActivity(), vo));
    }

    /**
     * make simple line graph
     * @return
     */
    private LineGraphVO makeLineGraphDefaultSetting() {

        String[] legendArr 	= {"1","2","3","4","5"};
        float[] graph1 		= {500,100,300,200,100};
        float[] graph2 		= {000,100,200,100,200};
        float[] graph3 		= {200,500,300,400,000};

        List<LineGraph> arrGraph 		= new ArrayList<LineGraph>();
        arrGraph.add(new LineGraph("Distance", 0xaa66ff33, graph1));
        arrGraph.add(new LineGraph("Time", 0xaa00ffff, graph2));
        arrGraph.add(new LineGraph("Calories", 0xaaff0066, graph3));

        LineGraphVO vo = new LineGraphVO(legendArr, arrGraph);
        return vo;
    }

    /**
     * make line graph using options
     * @return
     */
    private LineGraphVO makeLineGraphAllSetting() {
        //BASIC LAYOUT SETTING
        //padding
        int paddingBottom 	= LineGraphVO.DEFAULT_PADDING;
        int paddingTop 		= LineGraphVO.DEFAULT_PADDING;
        int paddingLeft 	= LineGraphVO.DEFAULT_PADDING;
        int paddingRight 	= LineGraphVO.DEFAULT_PADDING;

        //graph margin
        int marginTop 		= LineGraphVO.DEFAULT_MARGIN_TOP;
        int marginRight 	= LineGraphVO.DEFAULT_MARGIN_RIGHT;

        //max value
        int maxValue 		= LineGraphVO.DEFAULT_MAX_VALUE;

        //increment
        int increment 		= LineGraphVO.DEFAULT_INCREMENT;

        //GRAPH SETTING
        String[] legendArr 	= {"1","2","3","4","5"};
        float[] graph1 		= {500,100,300,200,100};
        float[] graph2 		= {400,250,200,100,300};

        List<LineGraph> arrGraph 		= new ArrayList<LineGraph>();

        //0xaa00ffff 청녹
        arrGraph.add(new LineGraph("Calories", 0xaaff0066, graph1));
        arrGraph.add(new LineGraph("Distance", 0xaa66ff33, graph2));

        LineGraphVO vo = new LineGraphVO(
                paddingBottom, paddingTop, paddingLeft, paddingRight,
                marginTop, marginRight, maxValue, increment, legendArr, arrGraph);

        //set animation
        vo.setAnimation(new GraphAnimation(GraphAnimation.LINEAR_ANIMATION, GraphAnimation.DEFAULT_DURATION));
        //set graph name box
        vo.setGraphNameBox(new GraphNameBox());
        //set draw graph region
//		vo.setDrawRegion(true);

        //use icon
//		arrGraph.add(new Graph(0xaa66ff33, graph1, R.drawable.icon1));
//		arrGraph.add(new Graph(0xaa00ffff, graph2, R.drawable.icon2));
//		arrGraph.add(new Graph(0xaaff0066, graph3, R.drawable.icon3));

//		LineGraphVO vo = new LineGraphVO(
//				paddingBottom, paddingTop, paddingLeft, paddingRight,
//				marginTop, marginRight, maxValue, increment, legendArr, arrGraph, R.drawable.bg);
        return vo;
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
        LinearLayout helathLayout = view.findViewById(R.id.Health_LinearLayout);
        m_layoutGraphView = (ViewGroup) view.findViewById(R.id.layoutGraphView);


        setLineGraph();


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