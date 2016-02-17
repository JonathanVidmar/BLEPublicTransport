package com.lth.thesis.blepublictransport;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;

/**
 * Created by Jonathan on 2/17/2016.
 */
public class NotificationHandler {
    private NotificationManager notificationManager;

    public NotificationHandler(Application application) {
        notificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
