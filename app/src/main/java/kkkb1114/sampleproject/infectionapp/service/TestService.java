package kkkb1114.sampleproject.infectionapp.service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import com.google.android.material.navigation.NavigationBarView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import kkkb1114.sampleproject.infectionapp.MainActivity;
import kkkb1114.sampleproject.infectionapp.Notification.AlarmReceiver;
import kkkb1114.sampleproject.infectionapp.R;
import kkkb1114.sampleproject.infectionapp.database.Bodytemp_DBHelper;
import kkkb1114.sampleproject.infectionapp.fragment.BodyTemperatureGraphFragment;
import kkkb1114.sampleproject.infectionapp.fragment.HomeFragment;
import kkkb1114.sampleproject.infectionapp.fragment.SettingFragment;
import kkkb1114.sampleproject.infectionapp.thermometer.Generator;
import kkkb1114.sampleproject.infectionapp.tools.PermissionManager;
import kkkb1114.sampleproject.infectionapp.tools.PreferenceManager;
import kkkb1114.sampleproject.infectionapp.tools.TimeCalculationManager;


public class TestService extends Service {

    Toolbar toolbar;
    Context context;
    Float flag = 0.0f;
    // 프래그먼트
    HomeFragment homeFragment;
    BodyTemperatureGraphFragment bodyTemperatureGraphFragment;
    SettingFragment settingFragment;

    // 바텀 네비게이션
    NavigationBarView navigationBarView;

    Handler handler = new Handler();
    Stack<Double> tempStack = new Stack<>();

    SharedPreferences select_user;

    //db
    public static Bodytemp_DBHelper bodytemp_dbHelper;
    SQLiteDatabase sqlDB;
    String username;
    String purpose;
    Cursor cursor;

    // 알람 관련
    AlarmManager alarmManager_high_tempreture;
    AlarmManager alarmManager_low_tempreture;
    AlarmManager alarmManager_inflammation_tempreture;
    PendingIntent pendingIntent;
    public static PowerManager.WakeLock wakeLock;
    // 알람 설정 쉐어드 값들
    // 감염
    String alarm_high_temperature_str; // 고온 알람 기준 값
    String alarm_low_temperature_str; // 저온 알람 기준 값
    boolean alarm_high_temperature_boolean; // 고온 알람 on / off
    boolean alarm_low_temperature_boolean; // 저온 알람 on / off
    boolean alarm_sound_temperature_boolean; // 사운드 알람 on / off
    // 염증
    String alarm_Inflammation_str;
    boolean alarm_Inflammation_boolean;
    boolean alarm_sound_inflammation_boolean; // 사운드 알람 on / off

    TimeCalculationManager timeCalculationManager;
    SharedPreferences preferences;
    String tempDateTime2 = "";
    String temp1;
    String temp2;
    private static String tempData = ""; // 다른 클래스에서 사용되는 현재 체온은 static으로 선언한다.

    int cnt = 0;

    private static final int NOTIFICATION_ID = 1;
    final String CHANNEL_ID = "hysorpatch_notification_channel";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.e("TestService", "onBind");
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        createNotificationChannel();

        // 이동하려는 액티비티를 작성해준다.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        // 노티에 전달 값을 담는다.
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent;

        // SDK 31 이상이면 PendingIntent.FLAG_IMMUTABLE (바뀐 정책상 무조건 FLAG_IMMUTABLE 사용해야함.)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("감염 체온")
                .setContentText("현재 체온 측정 기능이 동작하고 있습니다.")
                .setSmallIcon(R.drawable.pill)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);

        setUser();

        MeasurBodyTempreture();
    }
    public void setUser() {
        select_user = context.getSharedPreferences("login_user", MODE_PRIVATE);
        username = select_user.getString("userName", "선택된 사용자 없음");
        purpose = select_user.getString("userPurpose", "contaminate");

        if (username.equals("선택된 사용자 없음")) {
            Toast.makeText(getApplicationContext(), "사용자 등록을 완료해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    public void MeasurBodyTempreture() {

        // 체온측정 스레드 시작.
        new Thread(new Runnable() {
            @Override
            public void run() {

                // 1분이 지나면 최대값 파일에 작성
                if (tempStack.size() >= 20) {
                    Double avg = tempStack.peek();
                    Double cmp = 0.0;
                    while (!tempStack.isEmpty()) {
                        cmp += tempStack.pop();
                    }
                    avg = cmp / 20;
                    cmp = 0.0;
                    tempStack.clear();
                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String tempDateTime = dateFormat1.format(date);

                    sqlDB = MainActivity.bodytemp_dbHelper.getWritableDatabase();
                    sqlDB.execSQL("INSERT INTO TEMPDATA VALUES ('" + username + "', '" + avg + "', '" + tempDateTime + "');");
                    tempDateTime2 = tempDateTime;

                    if (purpose.equals("염증")) {
                        if (cnt == 0) {
                            temp1 = String.valueOf(avg);
                            cnt++;
                        } else if (cnt == 4) {
                            temp2 = String.valueOf(avg);
                            if (Float.valueOf(temp2) - Float.valueOf(temp1) >= 0)
                                // temp2: 현재 찍힌 체온, temp1: 5분 전에 찍힌 체온
                                try {
                                    Float nowTemp = Float.valueOf(temp2);
                                    Float beforeTemp = Float.valueOf(temp1);
                                    setAlarm_inflammation_elevated_bodyTemperature(String.format("%.2f", nowTemp), String.format("%.2f", beforeTemp));
                                    cnt = 0;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                        } else {
                            cnt++;
                        }
                    }
                }

                String s = "";

                // 3초마다 난수 받아옴
                if (purpose.equals("감염")) {
                    s = Generator.infection();
                } else if (purpose.equals("염증")) {
                    s = Generator.inflammation();

                    if (Float.valueOf(s) >= flag)
                        flag = Float.valueOf(s);
                    else
                        s = flag.toString();
                } else
                    s = Generator.ovulation();

                Log.d("temp: ", s);
                handler.postDelayed(this, 3000);
                tempStack.add(Double.valueOf(s));

                //todo 여기서 알람 체크 하기
                tempData = s; // 이건 static으로 현재 체온을 받아 다른 클래스에서 사용하기 위함이고 아래 tempData()메소드로 값을 받을 수 있다.
                setNotification(s);
                Log.d("------------", String.valueOf(tempStack.size()));
            }
        }).start();
    }

    public void setAlarm_inflammation_elevated_bodyTemperature(String nowTemp, String beforeTemp) {
        // 체온 상승에 대한 체크는 스레드에서 하기에 그냥 바로 울리면 된다.
        int requestID = (int) System.currentTimeMillis();

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarm_mode", 3); // 0: 고온, 1: 저온
        intent.putExtra("now_temperature", nowTemp);
        intent.putExtra("before_temperature", beforeTemp);

        PendingIntent pendingIntent;

        // SDK 31 이상이면 PendingIntent.FLAG_IMMUTABLE (바뀐 정책상 무조건 FLAG_IMMUTABLE 사용해야함.)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(context, requestID, intent,
                    PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context, requestID, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        AlarmManager alarmManager_administratione = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager_administratione.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
    }


    public void setNotification(String s) {
        PreferenceManager.PREFERENCES_NAME = "login_user";
        String select_user_name = PreferenceManager.getString(context, "userName");
        String userPurpose = PreferenceManager.getString(context, "userPurpose");

        if (select_user_name != null) {
            PreferenceManager.PREFERENCES_NAME = select_user_name + "Setting";

            switch (userPurpose) {
                case "감염":
                    // 고온 노티 체크
                    if (alarm_high_temperature_boolean) {
                        checkNotificationTemperature(alarm_sound_temperature_boolean, s, "high");
                    }
                    // 저온 노티 체크
                    if (alarm_low_temperature_boolean) {
                        checkNotificationTemperature(alarm_sound_temperature_boolean, s, "low");
                    }
                    break;
                case "염증":
                    if (alarm_Inflammation_boolean) {
                        checkNotificationTemperature(alarm_sound_inflammation_boolean, s, "Inflammation");
                    }
                    break;
                case "배란":

                    break;
            }
        }
    }

    public void checkNotificationTemperature(boolean isSoundAlarm, String s, String alarmType) {
        long requestID = System.currentTimeMillis();
        long now = getFormatTimeNow(requestID);

        if (alarmType.equals("high")) { // 고온 알람

            long alarm_high_temperature_term = PreferenceManager.getLong(context, "alarm_high_temperature_term_value");

            // 현재시간이 알람 텀 시간보다 클 경우 로직 동작
            if (now >= alarm_high_temperature_term) {

                double temperature_get = Double.parseDouble(alarm_high_temperature_str);
                double temperature_s = Double.parseDouble(s);

                if (temperature_get <= temperature_s) {

                    PreferenceManager.setBoolean(context, "alarm_high_temperature_boolean", false);
                    //PreferenceManager.setBoolean(context, "alarm_sound_temperature_boolean", false);
                    getAlarmCriteria();

                    long termTime = timeCalculationManager.getFormatTimeNow(PreferenceManager.getLong(context, "alarm_temperature_term"));
                    PreferenceManager.setLong(context, "alarm_high_temperature_term_value", termTime);

                    Intent intent = new Intent(context, AlarmReceiver.class);
                    intent.putExtra("now_temperature", s);
                    intent.putExtra("alarm_temperature", alarm_high_temperature_str);
                    intent.putExtra("alarm_mode", 0); // 0: 고온, 1: 저온
                    intent.putExtra("isSoundAlarm", isSoundAlarm);

                    // SDK 31 이상이면 PendingIntent.FLAG_IMMUTABLE (바뀐 정책상 무조건 FLAG_IMMUTABLE 사용해야함.)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), (int) requestID, intent,
                                PendingIntent.FLAG_IMMUTABLE);
                    } else {
                        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), (int) requestID, intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    alarmManager_high_tempreture.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
                }
            }
        } else if (alarmType.equals("low")) { // 저온 알람

            long alarm_low_temperature_term = PreferenceManager.getLong(context, "alarm_low_temperature_term_value");

            if (now >= alarm_low_temperature_term) {

                double temperature_get = Double.parseDouble(alarm_low_temperature_str);
                double temperature_s = Double.parseDouble(s);

                if (temperature_get >= temperature_s) {

                    PreferenceManager.setBoolean(context, "alarm_low_temperature_boolean", false);
                    //PreferenceManager.setBoolean(context, "alarm_sound_temperature_boolean", false);
                    getAlarmCriteria();

                    long nowNext = timeCalculationManager.getFormatTimeNow(PreferenceManager.getLong(context, "alarm_temperature_term"));
                    PreferenceManager.setLong(context, "alarm_low_temperature_term_value", nowNext);

                    Intent intent = new Intent(context, AlarmReceiver.class);
                    intent.putExtra("now_temperature", s);
                    intent.putExtra("alarm_temperature", alarm_low_temperature_str);
                    intent.putExtra("alarm_mode", 1); // 0: 고온, 1: 저온
                    intent.putExtra("isSoundAlarm", isSoundAlarm);

                    // SDK 31 이상이면 PendingIntent.FLAG_IMMUTABLE (바뀐 정책상 무조건 FLAG_IMMUTABLE 사용해야함.)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), (int) requestID, intent,
                                PendingIntent.FLAG_IMMUTABLE);
                    } else {
                        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), (int) requestID, intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    alarmManager_low_tempreture.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
                }
            }
        } else if (alarmType.equals("Inflammation")) { // 염증

            long alarm_inflammation_term = PreferenceManager.getLong(context, "alarm_relieve_inflammation_term_value");

            if (now >= alarm_inflammation_term) {

                double temperature_get = Double.parseDouble(alarm_Inflammation_str);
                double temperature_s = Double.parseDouble(s);

                if (temperature_get >= temperature_s) {

                    PreferenceManager.setBoolean(context, "alarm_inflammation_temperature_boolean", false);
                    //PreferenceManager.setBoolean(context, "alarm_sound_inflammation_boolean", false);
                    getAlarmCriteria();

                    long nowNext = timeCalculationManager.getFormatTimeNow(PreferenceManager.getLong(context, "alarm_temperature_term"));
                    PreferenceManager.setLong(context, "alarm_relieve_inflammation_term_value", nowNext);

                    Intent intent = new Intent(context, AlarmReceiver.class);
                    intent.putExtra("now_temperature", s);
                    intent.putExtra("alarm_temperature", alarm_Inflammation_str);
                    intent.putExtra("alarm_mode", 4); // 4: 염증 부위 체온 저하로 인한 완화
                    intent.putExtra("isSoundAlarm", isSoundAlarm);

                    // SDK 31 이상이면 PendingIntent.FLAG_IMMUTABLE (바뀐 정책상 무조건 FLAG_IMMUTABLE 사용해야함.)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), (int) requestID, intent,
                                PendingIntent.FLAG_IMMUTABLE);
                    } else {
                        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), (int) requestID, intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    alarmManager_inflammation_tempreture.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
                }
            }
        }
    }

    public long getFormatTimeNow(long time) {
        Date mReDate = new Date(time);
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formatDate = mFormat.format(mReDate);
        return Long.parseLong(formatDate);
    }

    public void getAlarmCriteria() {
        PreferenceManager.PREFERENCES_NAME = "login_user";
        String select_user_name = PreferenceManager.getString(context, "userName");
        if (select_user_name != null) {
            PreferenceManager.PREFERENCES_NAME = select_user_name + "Setting";
            // 감염
            alarm_high_temperature_str = PreferenceManager.getString(context, "alarm_high_temperature_value");
            alarm_low_temperature_str = PreferenceManager.getString(context, "alarm_low_temperature_value");
            alarm_high_temperature_boolean = PreferenceManager.getBoolean(context, "alarm_high_temperature_boolean");
            alarm_low_temperature_boolean = PreferenceManager.getBoolean(context, "alarm_low_temperature_boolean");
            alarm_sound_temperature_boolean = PreferenceManager.getBoolean(context, "alarm_sound_temperature_boolean");
            // 염증
            alarm_Inflammation_str = PreferenceManager.getString(context, "alarm_inflammation_temperature_value");
            alarm_Inflammation_boolean = PreferenceManager.getBoolean(context, "alarm_inflammation_temperature_boolean");
            alarm_sound_inflammation_boolean = PreferenceManager.getBoolean(context, "alarm_sound_inflammation_boolean");
        }
    }

    /**
     * 오레오 이상 버전부턴 startForegroundService()로 서비스 실행시 Notificaiton Channel과 Notification를 만들어 Notification를 등록해야한다.
     **/
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "hysorpatch_notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("감염 체온");

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void onDestroy() {
        super.onDestroy();

    }
}
