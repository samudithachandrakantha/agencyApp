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

import com.hfad.agencyapp.R;
import com.hfad.agencyapp.utils.Constants;

public class ChequeNotificationWorker extends Worker {

    public static final String PREFS_NAME = Constants.PREFS_CHEQUE;
    public static final String KEY_COUNT = Constants.KEY_CHEQUE_NOTIFICATION_COUNT;

    private static final String CHANNEL_ID = Constants.CHANNEL_CHEQUE_NOTIFICATIONS;

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
                .setContentTitle(ctx.getString(R.string.cheque_reminder_title))
                .setContentText(ctx.getString(R.string.cheque_reminder_message))
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
        prefs.edit().remove(Constants.KEY_NEXT_CHEQUE_NOTIFICATION_TEXT).apply();

        return Result.success();
    }

    private void createChannelIfNeeded(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, ctx.getString(R.string.cheque_channel_name), NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(ctx.getString(R.string.cheque_channel_description));
                nm.createNotificationChannel(channel);
            }
        }
    }
}

