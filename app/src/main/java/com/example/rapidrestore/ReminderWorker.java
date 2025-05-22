package com.example.rapidrestore;


import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {
    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String title = getInputData().getString("title");
        String message = getInputData().getString("message");
        String requestId = getInputData().getString("requestId");

        Intent intent = new Intent(getApplicationContext(), RequestDetailsActivity.class);
        intent.putExtra("requestId", requestId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        NotificationHelper.showNotification(
                getApplicationContext(),
                title,
                message,
                intent
        );

        return Result.success();
    }
}

