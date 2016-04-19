package com.lth.thesis.blepublictransport.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.lth.thesis.blepublictransport.Main.BLEPublicTransport;
import com.lth.thesis.blepublictransport.Main.MainActivity;
import com.lth.thesis.blepublictransport.R;

/**
 * A class to handle notifications sent by the application.
 *
 * @author      Jacob Arvidsson & Jonathan Vidmar
 * @version     1.1
 */
public class NotificationHandler {
    private NotificationManager notificationManager;
    private BLEPublicTransport application;

    public final static int VALID_TICKET_AVAILABLE = 0;
    public final static int NO_TICKET_AVAILABLE = 1;
    public final static int OPEN_GATE = 2;

    private final static int NOTIFICATION_ID = 1;

    public NotificationHandler(BLEPublicTransport application) {
        this.application = application;
        notificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void create() {
        if (application.hasValidTicket()){
            update(VALID_TICKET_AVAILABLE);
        } else {
            update(NO_TICKET_AVAILABLE);
        }
    }

    public void update(int type) {
        switch (type)
        {
            case VALID_TICKET_AVAILABLE:
                sendCustomNotification("ticket", R.drawable.ic_ticket, "Show ticket", "You have a valid ticket");
                break;
            case NO_TICKET_AVAILABLE:
                sendCustomNotification("payment", R.drawable.ic_add_ticket, "Buy ticket", "You currently don't have a valid ticket.");
                break;
            case OPEN_GATE:
                sendCustomNotification("gate", R.drawable.ic_ticket, "Open gate", "You are near a gate, open it by clicking the button.");
                break;
        }
    }

    private void sendCustomNotification(String intentName, int drawableId, String buttonTitle, String message){
        NotificationCompat.Builder builder = getBuilder();
        Intent fragmentIntent = new Intent(application, MainActivity.class);
        fragmentIntent.putExtra("fragment", intentName);
        PendingIntent pendingIntent = PendingIntent.getActivity(application.getApplicationContext(), 1, fragmentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action(drawableId, buttonTitle, pendingIntent);
        builder.addAction(action)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
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
        PendingIntent nearbyPendingIntent = PendingIntent.getActivity(application.getApplicationContext(), 0, nearbyFragmentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action nearbyAction = new NotificationCompat.Action(R.drawable.ic_location_on_black_24dp,"Nearby", nearbyPendingIntent);
        return new NotificationCompat.Builder(application)
                .setContentTitle("Welcome to Lunds Central Station")
                .setContentText("Swipe down to view options")
                .setSmallIcon(R.drawable.ic_notification_bus)
                .setColor(ContextCompat.getColor(application.getApplicationContext(), R.color.colorAccent))
                .setOngoing(true)
                .addAction(nearbyAction);
    }
}
