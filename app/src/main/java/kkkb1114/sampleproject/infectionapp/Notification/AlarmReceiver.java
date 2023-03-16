package kkkb1114.sampleproject.infectionapp.Notification;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import kkkb1114.sampleproject.infectionapp.MainActivity;
import kkkb1114.sampleproject.infectionapp.tools.PreferenceManager;
import kkkb1114.sampleproject.infectionapp.tools.TimeCalculationManager;

public class AlarmReceiver extends BroadcastReceiver {

    Context context;
    // 노티 변수
    NotificationManager_Tool temperatureNotification;
    String now_temperature; // 현제 체온
    String before_temperature; // 전 체온 (현재 전 체온은 염증에서만 측정이 된다)
    String alarm_temperature; // 알람 기준 체온
    int alarm_mode = 0; // 0: 고온, 1: 저온, 2: 투약 알람
    boolean isSoundAlarm = false; // 0: 고온, 1: 저온
    TimeCalculationManager timeCalculationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        timeCalculationManager = new TimeCalculationManager();

        PreferenceManager.PREFERENCES_NAME = "login_user";
        String select_user_name = PreferenceManager.getString(context, "userName");
        // 해당 화면에서는 알람값만 컨트롤하기에 "PREFERENCES_NAME" 설정
        PreferenceManager.PREFERENCES_NAME = select_user_name+"Setting";

        alarm_mode = intent.getIntExtra("alarm_mode", 0);
        now_temperature = intent.getStringExtra("now_temperature");
        before_temperature = intent.getStringExtra("before_temperature");
        alarm_temperature = intent.getStringExtra("alarm_temperature");
        isSoundAlarm = intent.getBooleanExtra("isSoundAlarm", false);

        // 어떤 알람인지 구분
        if (alarm_mode == 0){ // 감기/독감 (고온 알림)
            startNotification_HighTemperature(context);
        }else if (alarm_mode == 1){ // 감기/독감 (저온 알림)
            startNotification_LowTemperature(context);
        }else if (alarm_mode == 2){ // 투약 알림 (투약 알람은 사운드 안울림)
            startNotification_Administration(context);
        }else if (alarm_mode == 3){ // 염증 부위 체온 상승 알림
            startNotification_inflammation(context, alarm_mode);
        }else if (alarm_mode == 4){ // 염증 부위 체온 저하로 인한 완화 알림
            startNotification_inflammation(context, alarm_mode);
        }

        // 알람 사운드 추가 체크를 했어야 사운드 실행
        if (isSoundAlarm){
            playSound();
        }
        displayWakeUp();
    }

    /** 고온 Notification 설정 **/
    public void startNotification_HighTemperature(Context context){
        temperatureNotification = new NotificationManager_Tool(context);
        temperatureNotification.setNotification_HighTemperature(now_temperature, alarm_temperature);

        long now = timeCalculationManager.getFormatTimeNow(PreferenceManager.getLong(context, "alarm_temperature_term"));
        PreferenceManager.setLong(context, "alarm_high_temperature_term_value", now);
    }

    /** 저온 Notification 설정 **/
    public void startNotification_LowTemperature(Context context){
        temperatureNotification = new NotificationManager_Tool(context);
        temperatureNotification.setNotification_LowTemperature(now_temperature, alarm_temperature);

        long now = timeCalculationManager.getFormatTimeNow(PreferenceManager.getLong(context, "alarm_temperature_term"));
        PreferenceManager.setLong(context, "alarm_low_temperature_term_value", now);
    }

    /** 투약 Notification 설정 **/
    public void startNotification_Administration(Context context){
        temperatureNotification = new NotificationManager_Tool(context);
        temperatureNotification.setAdministrationAlarm();

        //todo 투약 알람 쉐어드 만들어야함.
        //PreferenceManager.setBoolean(context, "alarm_low_temperature_boolean", false);
    }

    /** 염증 Notification 설정 **/
    public void startNotification_inflammation(Context context, int inflammationAlarmMode){
        temperatureNotification = new NotificationManager_Tool(context);
        temperatureNotification.setInflammationAlarm(inflammationAlarmMode, now_temperature, before_temperature, alarm_temperature);

        // 염증 부위 체온 '저하'알람일 경우만 반복 알람 설정을 추가해준다.
        if (inflammationAlarmMode == 4){
            long now = timeCalculationManager.getFormatTimeNow(PreferenceManager.getLong(context, "alarm_temperature_term"));
            PreferenceManager.setLong(context, "alarm_relieve_inflammation_term_value", now);
        }
    }

    /** 사운드 시작 **/
    public void playSound(){
        // 사운드 서비스가 이미 동작중이면 제거하고 생성
        if (isAlarmServiceRunning()){
            stopSound();
        }

        // 감기
        if (alarm_mode <= 1){
            // 사운드 알람 체크 했는지 확인
            if (PreferenceManager.getBoolean(context, "alarm_sound_temperature_boolean")){
                Intent intent = new Intent(context, AlarmSoundService.class);
                // 오레오 이상은 startForegroundService() / 이하는 startService()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent);
                }else {
                    context.startService(intent);
                }
                // 알람이 한번 울리면 알람 설정을 off 한다.
                //PreferenceManager.setBoolean(context, "alarm_sound_temperature_boolean", false);
            }
        }else if (alarm_mode <= 4){// 염증
            // 사운드 알람 체크 했는지 확인
            if (PreferenceManager.getBoolean(context, "alarm_sound_inflammation_boolean")){
                Intent intent = new Intent(context, AlarmSoundService.class);
                // 오레오 이상은 startForegroundService() / 이하는 startService()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent);
                }else {
                    context.startService(intent);
                }
                // 알람이 한번 울리면 알람 설정을 off 한다.
                //PreferenceManager.setBoolean(context, "alarm_sound_inflammation_boolean", false);
            }
        }
    }

    /** 사운드 정지 **/
    public void stopSound(){
        Intent intent = new Intent(context, AlarmSoundService.class);
        context.stopService(intent);
    }

    /** 알람 서비스 동작 중인지 확인 **/
    public boolean isAlarmServiceRunning(){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)){
            if (AlarmSoundService.class.getName().equals(runningServiceInfo.getClass().getName())){
                return true;
            }
        }
        return false;
    }

    /** 화면 강제 기상 (환자를 상대로 알람을 울리는 것이기에 화면을 강제로 켤 필요가 있다고 보았다.) **/
    // todo PowerManager.FULL_WAKE_LOCK 대신 쓸 것을 찾아야함
    public void displayWakeUp(){
        MainActivity.wakeLock.acquire(); // 화면 즉시 기상
        MainActivity.wakeLock.release(); // wakeLock 자원 해제
    }
}