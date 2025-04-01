package com.example.watermyplants;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;

public class CountdownService extends Service {
    public static final String COUNTDOWN_BROADCAST = "com.example.COUNTDOWN_UPDATE";
    private static final String CHANNEL_ID = "CountdownServiceChannel";

    private CountDownTimer countDownTimer;
    private ArrayList<Long> timeList;
    private int currentIndex = 0;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::CountdownWakeLock");
        wakeLock.acquire();
        startForeground(1, getNotification("Countdown running..."));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel(); // Ensure notification channel exists

        Notification notification = new NotificationCompat.Builder(this, "myCh")
                .setContentTitle("Countdown Timer")
                .setContentText("Running in the background...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        startForeground(1, notification); // Run as a foreground service

        return START_STICKY;
    }

    private void startNextCountdown() {
        if (currentIndex >= timeList.size()) {
            stopSelf(); // Stop service when all countdowns are complete
            return;
        }

        long timeLeftInMillis = timeList.get(currentIndex);
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                sendBroadcastUpdate(millisUntilFinished, currentIndex);
            }

            @Override
            public void onFinish() {
                sendBroadcastUpdate(0, currentIndex);
                currentIndex++;
                startNextCountdown(); // Start the next countdown
            }
        }.start();
    }

    private void sendBroadcastUpdate(long timeLeft, int index) {
        Intent broadcastIntent = new Intent(COUNTDOWN_BROADCAST);
        broadcastIntent.putExtra("TIME_LEFT", timeLeft);
        broadcastIntent.putExtra("CURRENT_INDEX", index);
        sendBroadcast(broadcastIntent);
    }

    private Notification getNotification(String message) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Countdown Running")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID, "Countdown Service Channel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


