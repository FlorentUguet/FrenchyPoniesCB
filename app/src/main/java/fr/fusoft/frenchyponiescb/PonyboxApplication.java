package fr.fusoft.frenchyponiescb;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import fr.fusoft.frenchyponiescb.ponybox.Ponybox;
import fr.fusoft.frenchyponiescb.services.PonyboxService;
import fr.fusoft.frenchyponiescb.views.PonyboxActivity;

/**
 * Created by fuguet on 06/01/18.
 */

public class PonyboxApplication extends Application {
    private static final String LOG_TAG = "PonyboxApplication";

    private PonyboxService clientService;
    private boolean serviceBound = false;
    private ServiceConnectedListener mListener;
    private PonyboxActivity currentActivity;

    public interface ServiceConnectedListener{
        void onServiceConnected();
        void onServiceDisconnected();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Closing service", Toast.LENGTH_SHORT).show();
                }
            });
            stopService(new Intent(PonyboxApplication.this,PonyboxService.class));
            currentActivity.finishAffinity();

            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
        }
    };

    private final ServiceConnection clientServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PonyboxService.PonyboxBinder localBinder = (PonyboxService.PonyboxBinder)iBinder;
            clientService = localBinder.getService();
            Log.d(LOG_TAG, "Service connected");
            serviceBound = true;

            if(mListener != null)
                mListener.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            clientService = null;
            Log.d(LOG_TAG, "Service disconnected");
            serviceBound = false;

            if(mListener != null)
                mListener.onServiceDisconnected();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Intent serviceIntent = new Intent(this, PonyboxService.class);
        bindService(serviceIntent, clientServiceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter("android.intent.CLOSE_APPLICATION");
        registerReceiver(mReceiver, filter);

        Log.d(LOG_TAG, "Starting Application");
    }

    public void setCurrentActivity(PonyboxActivity activity){
        this.currentActivity = activity;
    }

    public void setListener(ServiceConnectedListener listener){
        mListener = listener;
    }

    public boolean isServiceBound(){
        return this.serviceBound;
    }

    public Ponybox getClient(){
        return clientService.getClient();
    }

    public boolean isBound() {
        return clientService != null;
    }

    public void quitApplication() {
        if (isBound()) {
            unbindService(clientServiceConnection);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}
