package com.lth.thesis.blepublictransport;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Jonathan on 2/17/2016.
 */
public class NotificationHandler {
    private NotificationManager notificationManager;
    private Application application;

    public final static int VALID_TICKET_AVAILABLE = 0;
    public final static int NO_TICKET_AVAILABLE = 1;

    private final static int NOTIFICATION_ID = 1;

    public NotificationHandler(Application application) {
        this.application = application;
        notificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
        init();
    }

    private void init() {
        update(NO_TICKET_AVAILABLE);
    }

    public void update(int type) {
        NotificationCompat.Builder builder = getBuilder();
        switch (type)
        {
            case VALID_TICKET_AVAILABLE:
                // add ticket fragment
                break;
            case NO_TICKET_AVAILABLE:
                Intent paymentFragmentIntent = new Intent(application, MainActivity.class);
                paymentFragmentIntent.putExtra("fragment", "payment");
                PendingIntent paymentPendingIntent = PendingIntent.getActivity(application.getApplicationContext(),1,paymentFragmentIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action paymentAction = new NotificationCompat.Action(R.drawable.ic_add_ticket, "Buy ticket", paymentPendingIntent);
                builder.addAction(paymentAction);
                break;
        }

        //attaches an activity when the user clicks the notification, which we don't want?
        //builder.setContentIntent(nearbyPendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void cancel() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private NotificationCompat.Builder getBuilder() {
        Intent nearbyFragmentIntent = new Intent(application, MainActivity.class);
        nearbyFragmentIntent.putExtra("fragment", "nearby");
        PendingIntent nearbyPendingIntent = PendingIntent.getActivity(application.getApplicationContext(),0,nearbyFragmentIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action nearbyAction = new NotificationCompat.Action(R.drawable.ic_location_on_black_24dp,"Nearby", nearbyPendingIntent);
        return new NotificationCompat.Builder(application)
                .setContentTitle("Welcome to King's Cross Station")
                .setContentText("Swipe down to view options")
                .setSmallIcon(R.drawable.ic_notification_bus)
                .setColor(ContextCompat.getColor(application.getApplicationContext(), R.color.colorAccent))
                .setOngoing(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("BLE Public Transport"))
                .addAction(nearbyAction);
    }
}
