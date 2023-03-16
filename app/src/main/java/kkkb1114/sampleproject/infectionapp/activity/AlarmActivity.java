package kkkb1114.sampleproject.infectionapp.activity;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import kkkb1114.sampleproject.infectionapp.R;
import kkkb1114.sampleproject.infectionapp.database.MyProfile.MyProfile;
import kkkb1114.sampleproject.infectionapp.database.MyProfile.MyProfile_DBHelper;
import kkkb1114.sampleproject.infectionapp.tools.PreferenceManager;
import kkkb1114.sampleproject.infectionapp.tools.TimeCalculationManager;

public class AlarmActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;

    MyProfile_DBHelper myProfile_dbHelper;
    MyProfile myProfile;
    FrameLayout fl_alarm;
    LinearLayout ll_infection;
    LinearLayout ll_inflammation;
    LinearLayout ll_ovulation;

    // 감염
    TextView tv_high_temperature_infection;
    TextView tv_low_temperature_infection;
    TextView tv_repeat_alarm_infection;
    SeekBar sb_high_temperature_infection;
    SeekBar sb_low_temperature_infection;
    Switch sw_high_temperature_infection;
    Switch sw_low_temperature_infection;
    Switch sw_alarm_add_sound_infection4;

    // 염증
    TextView tv_temperature_inflammation;
    TextView tv_repeat_alarm_inflammation;
    SeekBar sb_temperature_inflammation;
    Switch sw_temperature_inflammation;
    Switch sw_alarm_add_sound_inflammation;

    Button bt_alarm_confirm;
    Button bt_alarm_cancle;
    String select_user_name;
    String userPurpose; // 유저 앱 이용 목적

    TimeCalculationManager timeCalculationManager;
    DecimalFormat decimalFormat = new DecimalFormat(".#");

    /*
     * 1. 최초 화면 띄울때 뷰 세팅 구분 값
     * 2. 저장소에 알람 데이터가 있으면 프로그래스바가 움직여서 프로그래스바 움직임 감지 메소드가 돌기때문에 처음에는 막기위해 만들었다.
     */
    boolean firstSetView = false;

    // seekBar 최소, 최고 온도
    // 감기
    double max = 40.0;
    double min = 37.3;
    double alarm_high_temperature_value = 0.0;
    double alarm_low_temperature_value = 0.0;
    // 염증
    double alarm_inflammation_value = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        context = this;
        myProfile_dbHelper = new MyProfile_DBHelper();
        timeCalculationManager = new TimeCalculationManager();

        setPREFERENCES_NAME();

        initView();
        setSeekBar();
        setBodyTemperature();
        setScreen("감기/독감");
        getMyProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setBodyTemperature();
    }

    public void initView(){
        fl_alarm = findViewById(R.id.fl_alarm);
        ll_infection = findViewById(R.id.ll_infection);
        ll_inflammation = findViewById(R.id.ll_inflammation);
        // 감염
        tv_high_temperature_infection = findViewById(R.id.tv_high_temperature_infection);
        tv_low_temperature_infection = findViewById(R.id.tv_low_temperature_infection);
        tv_repeat_alarm_infection = findViewById(R.id.tv_repeat_alarm_infection);
        tv_repeat_alarm_infection.setOnClickListener(this);
        sb_high_temperature_infection = findViewById(R.id.sb_high_temperature_infection);
        sb_low_temperature_infection = findViewById(R.id.sb_low_temperature_infection);
        sw_high_temperature_infection = findViewById(R.id.sw_high_temperature_infection);
        sw_low_temperature_infection = findViewById(R.id.sw_low_temperature_infection);
        sw_alarm_add_sound_infection4 = findViewById(R.id.sw_alarm_add_sound_infection);
        // 염증
        tv_repeat_alarm_inflammation = findViewById(R.id.tv_repeat_alarm_inflammation);
        tv_repeat_alarm_inflammation.setOnClickListener(this);
        tv_temperature_inflammation = findViewById(R.id.tv_temperature_inflammation);
        sb_temperature_inflammation = findViewById(R.id.sb_temperature_inflammation);
        sw_temperature_inflammation = findViewById(R.id.sw_temperature_inflammation);
        sw_alarm_add_sound_inflammation = findViewById(R.id.sw_alarm_add_sound_inflammation);
        
        bt_alarm_confirm = findViewById(R.id.bt_alarm_confirm);
        bt_alarm_confirm.setOnClickListener(this);
        bt_alarm_cancle = findViewById(R.id.bt_alarm_cancle);
        bt_alarm_cancle.setOnClickListener(this);
    }

    /** Preference NAME 설정 **/
    public void setPREFERENCES_NAME(){
        PreferenceManager.PREFERENCES_NAME = "login_user";
        userPurpose = PreferenceManager.getString(context, "userPurpose");
        select_user_name = PreferenceManager.getString(context, "userName");
        // 해당 화면에서는 알람값만 컨트롤하기에 "PREFERENCES_NAME" 설정
        PreferenceManager.PREFERENCES_NAME = select_user_name+"Setting";
    }

    /** 현재 사용자 데이터 DB에서 가져오기 (사용 목적에 따라 알람 화면이 다르기 때문에 필요하다.) **/
    public void getMyProfile(){
        setPREFERENCES_NAME();
        myProfile = myProfile_dbHelper.DBselect(select_user_name);

        setScreen(myProfile.purpose);
    }

    /** 알람 화면이 사용 목적에 따라 UI가 달라지기에 매번 확인해야 한다. **/
    public void setScreen(String purpose){
        switch (purpose){
            case "감기/독감":
                ll_infection.setVisibility(View.VISIBLE);
                ll_inflammation.setVisibility(View.GONE);
                break;
            case "염증":
                ll_infection.setVisibility(View.GONE);
                ll_inflammation.setVisibility(View.VISIBLE);
                break;
            case "배란":
                break;
        }
    }

    /** 체온 처음 값 세팅 **/
    public void setBodyTemperature(){
        setPREFERENCES_NAME();

        if (userPurpose.equals("감염")){
            // 감염 (고체온)
            boolean alarm_high_temperature_boolean = PreferenceManager.getBoolean(context, "alarm_high_temperature_boolean");
            String alarm_high_temperature_str = PreferenceManager.getString(context, "alarm_high_temperature_value");
            int high_temperature_progress = 0;

            if (alarm_high_temperature_str.trim().isEmpty()){
                alarm_high_temperature_value = min;
                high_temperature_progress = (int) min;
            }else {
                alarm_high_temperature_value = Double.parseDouble(decimalFormat.format(Double.parseDouble(alarm_high_temperature_str)));
                high_temperature_progress = (int) ((alarm_high_temperature_value - min) * 100);
            }

            if (alarm_high_temperature_boolean){
                sw_high_temperature_infection.setChecked(true);
                tv_high_temperature_infection.setText(String.valueOf(alarm_high_temperature_value));
            }else {
                sw_high_temperature_infection.setChecked(false);
                tv_high_temperature_infection.setText(String.valueOf(min));
            }
            setProgressThumb(sb_high_temperature_infection, high_temperature_progress);

            // 감기 (저체온)
            boolean alarm_low_temperature_boolean = PreferenceManager.getBoolean(context, "alarm_low_temperature_boolean");
            String alarm_low_temperature_str = PreferenceManager.getString(context, "alarm_low_temperature_value");
            int low_temperature_progress = 0;

            if (alarm_low_temperature_str.trim().isEmpty()){
                alarm_low_temperature_value = min;
                low_temperature_progress = (int) min;
            }else {
                alarm_low_temperature_value = Double.parseDouble(decimalFormat.format(Double.parseDouble(alarm_low_temperature_str)));
                low_temperature_progress = (int) ((alarm_low_temperature_value - min) * 100);
            }

            if (alarm_low_temperature_boolean){
                sw_low_temperature_infection.setChecked(true);
                tv_low_temperature_infection.setText(String.valueOf(alarm_low_temperature_value));
            }else {
                sw_low_temperature_infection.setChecked(false);
                tv_low_temperature_infection.setText(String.valueOf(min));
            }
            setProgressThumb(sb_low_temperature_infection, low_temperature_progress);

        }else if (userPurpose.equals("염증")){
            // 염증
            boolean alarm_Inflammation_boolean = PreferenceManager.getBoolean(context, "alarm_inflammation_temperature_boolean");
            String alarm_Inflammation_str = PreferenceManager.getString(context, "alarm_inflammation_temperature_value");
            int inflammation_temperature_progress = 0;

            if (alarm_Inflammation_str.trim().isEmpty()){
                alarm_inflammation_value = min;
                inflammation_temperature_progress = (int) min;
            }else {
                alarm_inflammation_value = Double.parseDouble(decimalFormat.format(Double.parseDouble(alarm_Inflammation_str)));
                inflammation_temperature_progress = (int) ((alarm_inflammation_value - min) * 100);
            }

            if (alarm_Inflammation_boolean){
                sw_temperature_inflammation.setChecked(true);
                tv_temperature_inflammation.setText(String.valueOf(alarm_inflammation_value));
            }else {
                sw_temperature_inflammation.setChecked(false);
                tv_temperature_inflammation.setText(String.valueOf(min));
            }
            setProgressThumb(sb_temperature_inflammation, inflammation_temperature_progress);

            // todo 염증 (저체온)
        }

        // 사운드
        // 감기
        boolean alarm_sound_temperature_boolean = PreferenceManager.getBoolean(context, "alarm_sound_temperature_boolean");
        if (alarm_sound_temperature_boolean){
            sw_alarm_add_sound_infection4.setChecked(true);
        }else {
            sw_alarm_add_sound_infection4.setChecked(false);
        }

        // 염증
        boolean alarm_sound_inflammation_boolean = PreferenceManager.getBoolean(context, "alarm_sound_inflammation_boolean");
        if (alarm_sound_inflammation_boolean){
            sw_alarm_add_sound_inflammation.setChecked(true);
        }else {
            sw_alarm_add_sound_inflammation.setChecked(false);
        }

        long alarm_temperature_term = PreferenceManager.getLong(context, "alarm_temperature_term");
        if (alarm_temperature_term == -1L || alarm_temperature_term == timeCalculationManager.ten_MinutesMillis){
            tv_repeat_alarm_infection.setText("10분");
        }else if (alarm_temperature_term == timeCalculationManager.thirty_MinutesMillis){
            tv_repeat_alarm_infection.setText("30분");
        }else if (alarm_temperature_term == timeCalculationManager.sixty_MinutesMillis){
            tv_repeat_alarm_infection.setText("60분");
        }


        firstSetView = true;
    }

    /** seekBar 세팅 **/
    public void setSeekBar(){
        double step = 0.01;
        // seekbar 최대 값 설정
        if (userPurpose.equals("감염")){
            sb_high_temperature_infection.setMax((int) ((max-min) / step));
            sb_low_temperature_infection.setMax((int) ((max-min) / step));
            setSeekBarAnimation(sb_high_temperature_infection, "high_temperature_progress",max, min);
            setSeekBarAnimation(sb_low_temperature_infection, "low_temperature_progress",max, min);
            setSeekBarChange_high(min, step);
            setSeekBarChange_low(min, step);
        }else if (userPurpose.equals("염증")){
            sb_temperature_inflammation.setMax((int) ((max-min) / step));

            setSeekBarAnimation(sb_temperature_inflammation, "inflammation_temperature_progress",max, min);
            setSeekBarChange_inflammation(min, step);
        }
    }

    /** progress thumb 임의 위치로 배치 **/
    public void setProgressThumb(SeekBar seekBar, int progress){
        seekBar.post(new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(progress);
            }
        });
    }

    /** 고온 seekBar 동작시 이벤트 **/
    public void setSeekBarChange_high(double min, double step){
        sb_high_temperature_infection.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (firstSetView){
                    double value = min + (progress * step);
                    String result = decimalFormat.format(value);
                    tv_high_temperature_infection.setText(result);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /** 저온 seekBar 동작시 이벤트 **/
    public void setSeekBarChange_low(double min, double step){
        sb_low_temperature_infection.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (firstSetView){
                    double value = min + (progress * step);
                    String result = decimalFormat.format(value);
                    tv_low_temperature_infection.setText(result);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /** 염증 seekBar 동작시 이벤트 **/
    public void setSeekBarChange_inflammation(double min, double step){
        sb_temperature_inflammation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (firstSetView){
                    double value = min + (progress * step);
                    String result = decimalFormat.format(value);
                    tv_temperature_inflammation.setText(result);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /** seekBar 최초 위치 설정 **/
    public void setSeekBarAnimation(SeekBar seekBar, String propertyName,double max, double min){
        int progress_half = (int) (((max / min) / 2 ) -1);
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(seekBar, propertyName, progress_half);
        objectAnimator.setDuration(100); // 0.5초
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.start();
    }

    /** 알람 설정 데이터 저장 **/
    public void saveAlarmData(){
        setPREFERENCES_NAME();
        /*
         * 1. (고,저)체온 알람 스위치 on 되어 있는지 확인
         * 2.  on: (고,저)체온 알람 on 이라는 boolean 값 저장
         * 3.      (고,저)체온 기준 String 값 저장
         * 4. off: (고,저)체온 알람 off 이라는 boolean 값 저장
         *         이때는 값을 초기 값으로 저장한다.
         * 5. 사운드 추가 여부: on이면 알람 울릴때 음악이 흐르며 앱을 켜야 음악이 꺼진다.
         */
        // 고온 알람
        if (sw_high_temperature_infection.isChecked()){
            PreferenceManager.setBoolean(context, "alarm_high_temperature_boolean",true);
            PreferenceManager.setString(context, "alarm_high_temperature_value", tv_high_temperature_infection.getText().toString());
        }else {
            PreferenceManager.setBoolean(context, "alarm_high_temperature_boolean",false);
            PreferenceManager.setString(context, "alarm_high_temperature_value", String.valueOf(min));
        }
        PreferenceManager.setLong(context, "alarm_high_temperature_term_value", getFormatTimeNow());

        // 저온 알람
        if (sw_low_temperature_infection.isChecked()){
            PreferenceManager.setBoolean(context, "alarm_low_temperature_boolean",true);
            PreferenceManager.setString(context, "alarm_low_temperature_value", tv_low_temperature_infection.getText().toString());
        }else {
            PreferenceManager.setBoolean(context, "alarm_low_temperature_boolean",false);
            PreferenceManager.setString(context, "alarm_low_temperature_value", String.valueOf(min));
        }
        PreferenceManager.setLong(context, "alarm_low_temperature_term_value", getFormatTimeNow());

        // 염증 알람
        if (sw_temperature_inflammation.isChecked()){
            PreferenceManager.setBoolean(context, "alarm_inflammation_temperature_boolean",true);
            PreferenceManager.setString(context, "alarm_inflammation_temperature_value", tv_temperature_inflammation.getText().toString());
        }else {
            PreferenceManager.setBoolean(context, "alarm_inflammation_temperature_boolean",false);
            PreferenceManager.setString(context, "alarm_inflammation_temperature_value", String.valueOf(min));
        }
        PreferenceManager.setLong(context, "alarm_relieve_inflammation_term_value", getFormatTimeNow());

        // 사운드 추가 여부
        // 감기
        if (sw_alarm_add_sound_infection4.isChecked()){
            PreferenceManager.setBoolean(context, "alarm_sound_temperature_boolean",true);
        }else {
            PreferenceManager.setBoolean(context, "alarm_sound_temperature_boolean",false);
        }

        // 염증
        if (sw_alarm_add_sound_inflammation.isChecked()){
            PreferenceManager.setBoolean(context, "alarm_sound_inflammation_boolean",true);
        }else {
            PreferenceManager.setBoolean(context, "alarm_sound_inflammation_boolean",false);
        }
    }

    /** 현재 시간 구하기 **/
    public long getFormatTimeNow(){
        long requestID = System.currentTimeMillis();
        Date mReDate = new Date(requestID);
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMddHHmm");
        String formatDate = mFormat.format(mReDate);
        return Long.parseLong(formatDate);
    }

    /** 알람 텀 쉐어드 set **/
    public void setSharedAlarmTerm(String term){
        switch (term){
            case "10분":
                //todo 잠시 테스트용으로 1분짜리 만듬
                PreferenceManager.setLong(context, "alarm_temperature_term", timeCalculationManager.one_MinutesMillis);
                //PreferenceManager.setLong(context, "alarm_temperature_term", timeCalculationManager.ten_MinutesMillis);
                break;
            case "30분":
                PreferenceManager.setLong(context, "alarm_temperature_term", timeCalculationManager.thirty_MinutesMillis);
                break;
            case "60분":
                PreferenceManager.setLong(context, "alarm_temperature_term", timeCalculationManager.sixty_MinutesMillis);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_repeat_alarm_infection:
            case R.id.tv_repeat_alarm_inflammation:
                String[] items = new String[]{"10분", "30분", "60분"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("알람이 반복될 시간 간격을 선택해주세요.");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        String selectedItem = items[position];
                        tv_repeat_alarm_infection.setText(selectedItem);
                    }
                });
                builder.show();
                break;

            case R.id.bt_alarm_cancle:
                finish();
                break;
            case R.id.bt_alarm_confirm:
                saveAlarmData();
                setSharedAlarmTerm(tv_repeat_alarm_infection.getText().toString());
                finish();
                break;
        }
    }
}