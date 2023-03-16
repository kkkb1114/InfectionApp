package kkkb1114.sampleproject.infectionapp.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import kkkb1114.sampleproject.infectionapp.MainActivity;
import kkkb1114.sampleproject.infectionapp.R;
import kkkb1114.sampleproject.infectionapp.SplashActivity;

public class NotificationManager_Tool {

    Context context;
    public static final String NOTIFICATION_CHANNEL_ID_TEMPERATURE_HIGH = "10001"; // 고온 알람 노티 채널 ID
    public static final String NOTIFICATION_CHANNEL_ID_TEMPERATURE_LOW = "10002"; // 고온 알람 노티 채널 ID
    public static final String NOTIFICATION_CHANNEL_ID_TEMPERATURE_Administration = "10003"; // 투약 알람 노티 채널 ID

    public NotificationManager_Tool(Context context){
        this.context = context;
    }

    /** 고온 Notification 설정 **/
    public void setNotification_HighTemperature(String now_temperature, String high_temperature){

        // 채널을 생성 및 전달해 줄수 있는 NotificationManager 생성
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 이동하려는 액티비티를 작성해준다.
        Intent notificationIntent = new Intent(context, SplashActivity.class);
        // 노티에 전달 값을 담는다.
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent;

        // SDK 31 이상이면 PendingIntent.FLAG_IMMUTABLE (바뀐 정책상 무조건 FLAG_IMMUTABLE 사용해야함.)
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
            pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }else {
            pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // 고온 노티
        NotificationCompat.Builder builder_high_temperature = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_TEMPERATURE_HIGH)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.pill))
                .setContentTitle("고온 알림 (현재 체온: "+now_temperature+"°C)")
                .setContentText("현재 체온이 "+ high_temperature +"°C 를 넘었습니다.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 노티 클릭시 위에 생성한 PendingIntent 실행 (설정한 데이터 담고 MainActivity로 이동)
                .setAutoCancel(true); // 눌러야 꺼짐

        // OREO API 26 이상부터 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder_high_temperature.setSmallIcon(R.drawable.pill);
            CharSequence channelName = "channel_notification_high_temperature";
            String description = "오레오 이상";
            int importance = NotificationManager.IMPORTANCE_HIGH; // 우선순위 설정

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_TEMPERATURE_HIGH,
                    channelName, importance);
            notificationChannel.setDescription(description);

            // 노티피케이션 채널 시스템에 등록
            if (notificationManager != null){
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }else {
            builder_high_temperature.setSmallIcon(R.mipmap.ic_launcher_round); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남
        }

        if (notificationManager != null){
            notificationManager.notify(1234, builder_high_temperature.build());
        }
    }

    /** 저온 Notification 설정 **/
    public void setNotification_LowTemperature(String now_temperature, String low_temperature){
        // 채널을 생성 및 전달해 줄수 있는 NotificationManager 생성
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 이동하려는 액티비티를 작성해준다.
        Intent notificationIntent = new Intent(context, SplashActivity.class);
        // 노티에 전달 값을 담는다.
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent;
        // SDK 31 이상이면 PendingIntent.FLAG_IMMUTABLE (바뀐 정책상 무조건 FLAG_IMMUTABLE 사용해야함.)
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
            pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }else {
            pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // 저온 노티
        NotificationCompat.Builder builder_low_temperature = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_TEMPERATURE_LOW)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.pill))
                .setContentTitle("저온 알림 (현재 체온: "+now_temperature+"°C)")
                .setContentText("현재 체온이 "+ low_temperature +"°C 이하로 떨어졌습니다.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 노티 클릭시 위에 생성한 PendingIntent 실행 (설정한 데이터 담고 MainActivity로 이동)
                .setAutoCancel(true); // 눌러야 꺼짐

        // OREO API 26 이상부터 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder_low_temperature.setSmallIcon(R.drawable.pill);
            CharSequence channelName = "channel_notification_low_temperature";
            String description = "오레오 이상";
            int importance = NotificationManager.IMPORTANCE_HIGH; // 우선순위 설정

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_TEMPERATURE_LOW,
                    channelName, importance);
            notificationChannel.setDescription(description);

            // 노티피케이션 채널 시스템에 등록
            if (notificationManager != null){
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }else {
            builder_low_temperature.setSmallIcon(R.mipmap.ic_launcher_round); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남
        }

        if (notificationManager != null){
            notificationManager.notify(12345, builder_low_temperature.build());
        }
    }

    /** 투약 알람 세팅 **/
    public void setAdministrationAlarm(){
        // 채널을 생성 전달할 NotificationManager 생성x
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 전달할 intent 작성
        Intent notificationIntent = new Intent(context, SplashActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent;
        // SDK 31 이상이면 PendingIntent.FLAG_IMMUTABLE (바뀐 정책상 무조건 FLAG_IMMUTABLE 사용해야함.)
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
            pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }else {
            pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // 투약 노티
        String tempData = MainActivity.getTempData(); // MainActivity에서 현재 체온을 받아온다.
        NotificationCompat.Builder builder;
        if (tempData.trim().isEmpty()){
            builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_TEMPERATURE_Administration)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.pill))
                    .setContentTitle("투약 알림")
                    .setContentText("투약한지 30분이 경과 되었습니다.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }else {
            builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_TEMPERATURE_Administration)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.pill))
                    .setContentTitle("투약 알림 (현재 체온: "+tempData+"°C)")
                    .setContentText("투약한지 30분이 경과 되었습니다.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }


        // SDK OREO API 26이상부터 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder.setSmallIcon(R.drawable.pill);
            CharSequence channelName = "channel_notification_administration";
            String description = "오레오 이상";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_TEMPERATURE_Administration,
                    channelName, importance);
            notificationChannel.setDescription(description);

            // 노티피케이션 채널 시스템에 등록
            if (notificationManager != null){
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }else {
            builder.setSmallIcon(R.mipmap.ic_launcher_round); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남
        }

        if (notificationManager != null){
            notificationManager.notify(123456, builder.build());
        }
    }

    /** 염증 악화, 완화 알람 세팅 **/
    public void setInflammationAlarm(int inflammationAlarmMode, String now_temperature, String before_temperature, String low_temperature){
        // 채널을 생성 전달할 NotificationManager 생성x
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 전달할 intent 작성
        Intent notificationIntent = new Intent(context, SplashActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent;

        // SDK 31 이상이면 PendingIntent.FLAG_IMMUTABLE (바뀐 정책상 무조건 FLAG_IMMUTABLE 사용해야함.)
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
            pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }else {
            pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }


        NotificationCompat.Builder builder;

        /*
         * 투약 노티 생성
         * 1. inflammationAlarmMode = 3: 염증 부위 악화, 4: 염증 체온 저하로 인한 완화
         */
        if (inflammationAlarmMode == 3){
            String calculatedTemp = String.format("%.2f",(Float.parseFloat(now_temperature) - Float.parseFloat(before_temperature)));
            builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_TEMPERATURE_Administration)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.pill))
                    .setContentTitle("염증 알림 (현재 체온: "+now_temperature+"°C)")
                    .setContentText("30분간 체온이 "+ calculatedTemp +"°C 상승 하였습니다.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }else if (inflammationAlarmMode == 4){
            builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_TEMPERATURE_Administration)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.pill))
                    .setContentTitle("염증 알림 (현재 체온: "+now_temperature+"°C)")
                    .setContentText("현재 체온이 "+ low_temperature +"°C 이하로 떨어졌습니다.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }else {
            builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_TEMPERATURE_Administration)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.pill))
                    .setContentTitle("염증 부위 체온 '저하' 알림")
                    .setContentText("염증 부위의 체온이 저하 되었습니다.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }


        // SDK OREO API 26이상부터 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder.setSmallIcon(R.drawable.pill);
            CharSequence channelName = "channel_notification_administration";
            String description = "오레오 이상";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_TEMPERATURE_Administration,
                    channelName, importance);
            notificationChannel.setDescription(description);

            // 노티피케이션 채널 시스템에 등록
            if (notificationManager != null){
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }else {
            builder.setSmallIcon(R.mipmap.ic_launcher_round); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남
        }

        if (notificationManager != null){
            notificationManager.notify(1234567, builder.build());
        }
    }
}
