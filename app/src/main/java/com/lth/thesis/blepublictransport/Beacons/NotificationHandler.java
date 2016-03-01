package com.lth.thesis.blepublictransport.Beacons;

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
 * Created by Jonathan on 2/17/2016.
 */
public class NotificationHandler {
    private NotificationManager notificationManager;
    private BLEPublicTransport application;

    public final static int VALID_TICKET_AVAILABLE = 0;
    public final static int NO_TICKET_AVAILABLE = 1;

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
        NotificationCompat.Builder builder = getBuilder();
        switch (type)
        {
            case VALID_TICKET_AVAILABLE:
                Intent ticketFragmentIntent = new Intent(application, MainActivity.class);
                ticketFragmentIntent.putExtra("fragment", "ticket");
                PendingIntent ticketPendingIntent = PendingIntent.getActivity(application.getApplicationContext(),1,ticketFragmentIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action ticketAction = new NotificationCompat.Action(R.drawable.ic_ticket, "Show ticket", ticketPendingIntent);
                builder.addAction(ticketAction)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("You have a valid ticket."));
                break;
            case NO_TICKET_AVAILABLE:
                Intent paymentFragmentIntent = new Intent(application, MainActivity.class);
                paymentFragmentIntent.putExtra("fragment", "payment");
                PendingIntent paymentPendingIntent = PendingIntent.getActivity(application.getApplicationContext(),1,paymentFragmentIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action paymentAction = new NotificationCompat.Action(R.drawable.ic_add_ticket, "Buy ticket", paymentPendingIntent);
                builder.addAction(paymentAction)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("You currently don't have a valid ticket."));
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
                .addAction(nearbyAction);
    }
}
