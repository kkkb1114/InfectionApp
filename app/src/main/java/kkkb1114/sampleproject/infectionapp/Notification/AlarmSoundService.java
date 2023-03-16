package kkkb1114.sampleproject.infectionapp.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import kkkb1114.sampleproject.infectionapp.R;
import kkkb1114.sampleproject.infectionapp.SplashActivity;

public class AlarmSoundService extends Service {

    MediaPlayer mediaPlayer;
    final String CHANNEL_ID = "Alarm_sound_notification_channel";
    final int NOTIFICATION_ID = 10;
    Notification notification;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        // 이동하려는 액티비티를 작성해준다.
        Intent notificationIntent = new Intent(getApplicationContext(), SplashActivity.class);
        // 노티에 전달 값을 담는다.
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent;

        // SDK 31 이상이면 PendingIntent.FLAG_IMMUTABLE (바뀐 정책상 무조건 FLAG_IMMUTABLE 사용해야함.)
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE);
        }else {
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("체온 알람 사운드")
                .setContentText("체온 알람 사운드가 실행 중입니다.")
                .setContentIntent(pendingIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
        mediaPlayer = MediaPlayer.create(this, R.raw.ouu);
        mediaPlayer.setLooping(false); // 반복재생 false
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer.start();
        return super.onStartCommand(intent, flags, startId);
    }

    /** 오레오 이상 버전부턴 startForegroundService()로 서비스 실행시 Notificaiton Channel과 Notification를 만들어 Notification를 등록해야한다. **/
    public void createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm_sound_notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("하하하하");

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        stopForeground(true);
    }
}
