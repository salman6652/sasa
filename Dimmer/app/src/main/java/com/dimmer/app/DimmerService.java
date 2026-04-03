package com.dimmer.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import androidx.core.app.NotificationCompat;

public class DimmerService extends Service {

    static DimmerService INSTANCE;
    private static final String CHANNEL_ID = "DimmerChannel";

    private static final int NOTIFICATION_ID = 101;
    private static final String PREFS = "DimmerPrefs";

    private View dimView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams overlayParams;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        INSTANCE = this;
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        createNotification();

        dimView = new View(this);
        int alpha = prefsGetInt("alpha", 128);
        dimView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));

        overlayParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT);
        overlayParams.gravity = Gravity.TOP | Gravity.START;
        windowManager.addView(dimView, overlayParams);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dimView != null) {
            try {
                windowManager.removeViewImmediate(dimView);
            } catch (Exception e) { /* ignore */ }
            dimView = null;
        }
    }

    private int prefsGetInt(String key, int defValue) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        return prefs.getInt(key, defValue);
    }

    private void prefsPutInt(String key, int value) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(key, value).apply();
    }

    /** Called by MainActivity to update the overlay alpha without restarting. */
    public static void updateAlpha(Context context, int alpha) {
        DimmerService service = INSTANCE;
        if (service != null && service.dimView != null) {
            service.dimView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt("alpha", alpha).apply();
    }

    private void createNotification() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Dimmer", NotificationManager.IMPORTANCE_MIN);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(channel);

        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Dimmer Active")
                .setContentText("Screen is dimmed")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }
}