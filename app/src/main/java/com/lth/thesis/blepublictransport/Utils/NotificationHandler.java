package com.lth.thesis.blepublictransport.Utils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.lth.thesis.blepublictransport.BluetoothClient.BluetoothClient;
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
    public static final String ACTION_1 = "action_1";


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
                //application.shouldNotify = true;
                break;
            case NO_TICKET_AVAILABLE:
                sendCustomNotification("payment", R.drawable.ic_add_ticket, "Buy ticket", "You currently don't have a valid ticket.");
                //application.shouldNotify = true;
                break;
            case OPEN_GATE:
                //sendBackgroundNotification("Open gate", "You are near a gate, open it by clicking the button.", R.drawable.ic_ticket);
                Log.d("Note", "Wnyyy??");
                displayBackgroundNotification(application);
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
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
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

    public void displayBackgroundNotification(Context context) {

        Intent intent = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_1)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(application);
        builder.setContentTitle("Welcome to Lunds Central Station")
                .setContentText("You are approaching a gate, pleace click to open the gate.")
                .setSmallIcon(R.drawable.ic_notification_bus)
                .setColor(ContextCompat.getColor(application.getApplicationContext(), R.color.colorAccent))
                .setOngoing(true)
                .setSound(alarmSound)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        public void onCreate() {
            super.onCreate();
            Log.d("Server", ">>>onCreate()");
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            super.onStartCommand(intent, startId, startId);
            Log.i("LocalService", "Received start id " + startId + ": " + intent);

            return START_STICKY;
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            String action = intent.getAction();
            //DebugUtils.log("Received notification action: " + action);
            Log.d("Note", "yoklj");

            if (ACTION_1.equals(action)) {
                // TODO: handle action 1.
                Log.d("Note", "yo");
                BLEPublicTransport application = (BLEPublicTransport) getApplication();
                application.manageGate(BluetoothClient.MESSAGE_OPEN_TIMER);
                // If you want to cancel the notification: NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
                new NotificationHandler(application).update(NotificationHandler.VALID_TICKET_AVAILABLE);
            }
        }
    }
}
