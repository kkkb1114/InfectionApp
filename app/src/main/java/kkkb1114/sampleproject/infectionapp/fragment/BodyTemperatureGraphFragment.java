package kkkb1114.sampleproject.infectionapp.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import kkkb1114.sampleproject.infectionapp.MainActivity;
import kkkb1114.sampleproject.infectionapp.R;
import kkkb1114.sampleproject.infectionapp.timeline.TimelineAdapter;


public class BodyTemperatureGraphFragment extends Fragment {

    public LineChart lineChart;
    Context context;
    RecyclerView rv_timeline;
    TimelineAdapter timelineAdapter;
    TextView tv_graphdate;




    LinearLayout period_layout;
    MaterialDatePicker materialDatePicker;
    String username;

    View view;

    SharedPreferences select_user;

    SQLiteDatabase sqlDB;

    Cursor cursor;







    public BodyTemperatureGraphFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        ArrayList<Entry> entry_chart = new ArrayList<>();

        setUser();
        View view = inflater.inflate(R.layout.fragment_body_temperature_graph, container, false);
        try {
            initView(view);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        this.setListner();

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        tv_graphdate.setText(dateFormat.format(date));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUser();
    }

    public void initView(View view) throws ParseException {
        tv_graphdate=view.findViewById(R.id.tv_graphdate);
        lineChart = view.findViewById(R.id.chart);
        rv_timeline=view.findViewById(R.id.rv_timeline_list);


        long now =System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String str = dateFormat.format(date);

        String userpurpose = select_user.getString("userPurpose"," ");

        if(userpurpose.equals("감염"))
        {
            tv_graphdate.setVisibility(View.VISIBLE);
            rv_timeline.setVisibility(View.VISIBLE);
            lineChart.setVisibility(View.VISIBLE);
            showChart(view,str);
            setRecyclerView(view,str);

        }

    }

    public void showChart(View view , String date) {

        sqlDB = MainActivity.bodytemp_dbHelper.getReadableDatabase();
        HashMap<String,Double> keys = new HashMap<String, Double>();
        lineChart = new LineChart(getActivity());
        lineChart = view.findViewById(R.id.chart);
        XAxis xAxis = lineChart.getXAxis();
        String username = select_user.getString("userName","선택된 사용자 없음");

        cursor = sqlDB.rawQuery("SELECT * FROM TEMPDATA WHERE tempDateTime LIKE '"+date+"%' AND name = '"+username+"'; ", null);

        while(cursor.moveToNext()) {
            keys.put(cursor.getString(2).substring(11), cursor.getDouble(1));
        }

        if(keys.size()==0) {
            lineChart.setData(null);
            lineChart.invalidate();
            return;
        }

        LineData lineData = new LineData();

        ArrayList<Entry> entry_chart_Y = new ArrayList<>();
        ArrayList<String> entry_chart_X = new ArrayList<>();
        Comparator<String> comparator = (s1, s2) -> s1.compareTo(s2);
        Map<String, String> map = new TreeMap<>(comparator);

        for(Map.Entry<String,?> entry : keys.entrySet()){
            String key = String.valueOf(entry.getKey());
            String value = String.valueOf(entry.getValue());

            map.put(key,value);
        }


        int i=0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key=entry.getKey();
            float value=Float.valueOf(entry.getValue());
            entry_chart_Y.add(new Entry(i,value));
            entry_chart_X.add(key);
            i++;
        }



        LineDataSet lineDataSet = new LineDataSet(entry_chart_Y, "체온"); // 데이터가 담긴 Arraylist 를 LineDataSet 으로 변환한다.
        lineDataSet.setColor(Color.RED); // 해당 LineDataSet의 색 설정 :: 각 Line 과 관련된 세팅은 여기서 설정한다.
        lineDataSet.setLineWidth(3);


        XAxis.XAxisPosition position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setPosition(position);// x 축 설정
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(entry_chart_X));
        lineData.addDataSet(lineDataSet); // 해당 LineDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다.
        lineChart.setData(lineData); // 차트에 위의 DataSet을 넣는다.
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate(); // 차트 업데이트
        lineChart.setTouchEnabled(false);


    }

    public void setRecyclerView(View view,String date){
        sqlDB = MainActivity.bodytemp_dbHelper.getReadableDatabase();
        ArrayList<String> ad = new ArrayList<>();
        HashMap<String,String> keys = new HashMap<String, String>();
        ArrayList<Entry> entry_arr = new ArrayList<>();
        rv_timeline=view.findViewById(R.id.rv_timeline_list);
        rv_timeline.setVisibility(View.VISIBLE);

        String username = select_user.getString("userName","선택된 사용자 없음");
        cursor = sqlDB.rawQuery("SELECT * FROM TIMELINEDATA WHERE TimelineDateTime LIKE '"+date+"%' AND name = '"+username+"'; ", null);

        while(cursor.moveToNext()) {
            keys.put(cursor.getString(2).substring(11), cursor.getString(1));
        }

        if(keys.size()==0) {
            rv_timeline.setVisibility(View.INVISIBLE);
            return;
        }



        timelineAdapter = new TimelineAdapter(ad);
        rv_timeline.setLayoutManager(new LinearLayoutManager(context));
        rv_timeline.setAdapter(new TimelineAdapter(ad));
        timelineAdapter.notifyDataSetChanged();

        Comparator<String> comparator = (s1, s2) -> s1.compareTo(s2);
        Map<String, String> map = new TreeMap<>(comparator);

        for(Map.Entry<String,?> entry : keys.entrySet()){
            String key = String.valueOf(entry.getKey());
            String value = String.valueOf(entry.getValue());

            map.put(key,value);
        }


        int i=0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value=entry.getValue();
            ad.add(key+" "+value);
            i++;
        }

    }

    public void setListner(){
        tv_graphdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMaterialDatePicker();
                MaterialDatePickerClick();
            }
        });


    }

    public void showMaterialDatePicker(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        // 오늘 날짜
        Long today = MaterialDatePicker.todayInUtcMilliseconds();

        materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Date Picker")
                .setSelection(today).build();

        materialDatePicker.show(getActivity().getSupportFragmentManager(), "DatePicker");
    }

    public void MaterialDatePickerClick(){
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date();
                date.setTime(selection);

                String dateStr = simpleDateFormat.format(date);
                tv_graphdate.setText(dateStr);

                select_user = context.getSharedPreferences("login_user",MODE_PRIVATE);
                String username = select_user.getString("userName","선택된 사용자 없음");


                showChart(getView(),dateStr);
                setRecyclerView(getView(),dateStr);
            }
        });
    }

    public void setUser()
    {
        context = getContext();


        select_user = context.getSharedPreferences("login_user",MODE_PRIVATE);
        String username = select_user.getString("userName","선택된 사용자 없음");


        if(username.equals("선택된 사용자 없음"))
        {
            Toast.makeText(context, "사용자 등록을 완료해주세요.", Toast.LENGTH_SHORT).show();
        }

    }






}