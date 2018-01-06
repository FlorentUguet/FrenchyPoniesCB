package fr.fusoft.frenchyponiescb.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import fr.fusoft.frenchyponiescb.ponybox.Ponybox;
import fr.fusoft.frenchyponiescb.views.PonyboxActivity;
import fr.fusoft.frenchyponiescb.R;

/**
 * Created by fuguet on 05/01/18.
 */

public class PonyboxService extends Service {
    private NotificationManager mNM;
    private Notification.Builder mBuilder;

    private static final String LOG_TAG = "FClientService";
    private static final String SERVER_URL = "http://94.23.60.187:8080";

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.ponybox_started;
    private static final int notificationId = 1;

    private Ponybox client;

    public Ponybox getClient(){
        return this.client;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG,"Service Created");
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification(R.string.ponybox_service_label, R.string.ponybox_started);
    }

    public void stopService(){
        Log.i(LOG_TAG,"Request for closing the service");
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.ponybox_closed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new PonyboxBinder();

    public class PonyboxBinder extends Binder {
        public PonyboxService getService(){
            return PonyboxService.this;
        }
    }

    private void showNotification(int label, int content) {
        showNotification(getText(label), getText(content));
    }

    private void showNotification(CharSequence label, CharSequence content) {

        if(this.mBuilder == null){
            // The PendingIntent to launch our activity if the user selects this notification
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, PonyboxActivity.class), 0);
            PendingIntent exitIntent = PendingIntent.getBroadcast(this, 0, new Intent("android.intent.CLOSE_APPLICATION"), 0);

            // Set the info for the views that show in the notification panel.
            this.mBuilder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_service)
                    .setTicker(label)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentTitle(label)  // the label of the entry
                    .setContentText(content)  // the contents of the entry
                    .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                    .addAction(R.drawable.ic_notif_exit, getText(R.string.exit), exitIntent);

            // Send the notification.

            startForeground(notificationId, this.mBuilder.build());
        }else{
            this.mBuilder
                    .setContentTitle(label)  // the label of the entry
                    .setContentText(content);

            mNM.notify(notificationId, this.mBuilder.build());
        }

    }
}
