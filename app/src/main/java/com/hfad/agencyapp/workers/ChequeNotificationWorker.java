package com.hfad.agencyapp.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ChequeNotificationWorker extends Worker {

    public static final String PREFS_NAME = "cheque_prefs";
    public static final String KEY_COUNT = "cheque_notification_count";

    private static final String CHANNEL_ID = "cheque_notifications";

    public ChequeNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();

        createChannelIfNeeded(ctx);

        // Build a simple notification
        NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Cheque Reminder")
                .setContentText("A cheque is expected to clear. Please follow up.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify((int) System.currentTimeMillis(), nb.build());
        }

        // Increment stored badge count
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int count = prefs.getInt(KEY_COUNT, 0);
        prefs.edit().putInt(KEY_COUNT, count + 1).apply();

        // Clear any 'next notification' entry - UI will refresh based on count
        prefs.edit().remove("next_cheque_notification_text").apply();

        return Result.success();
    }

    private void createChannelIfNeeded(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Cheque reminders", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Notifications for cheque clearance reminders");
                nm.createNotificationChannel(channel);
            }
        }
    }
}

