package fr.fusoft.frenchyponiescb.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.fusoft.frenchyponiescb.PonyboxApplication;
import fr.fusoft.frenchyponiescb.R;
import fr.fusoft.frenchyponiescb.controllers.LoginTask;
import fr.fusoft.frenchyponiescb.ponybox.Channel;
import fr.fusoft.frenchyponiescb.ponybox.Ponybox;

public class PonyboxActivity extends AppCompatActivity {
    final Context context = this;
    Dialog loginDialog;
    PonyboxApplication app = null;
    Ponybox client = null;
    List<ChannelFragment> fragments = new ArrayList<>();

    String LOG_TAG = "PonyboxActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }

    @Override
    protected void onStart() {
        super.onStart();

        this.app = (PonyboxApplication) getApplication();
        this.app.setCurrentActivity(this);
        this.app.setListener(new PonyboxApplication.ServiceConnectedListener() {
            @Override
            public void onServiceConnected() {
                client = app.getClient();
                initClientListener();
                showLoginDialog();
            }

            @Override
            public void onServiceDisconnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PonyboxActivity.this, "Service unbound", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        if (app.isServiceBound()) {
            this.client = this.app.getClient();
            initClientListener();
        }
    }

    public void initClientListener(){

        if(this.client == null){
            Log.e(LOG_TAG, "Error : The client is not initialized");
            return;
        }

        this.client.setListener(new Ponybox.PonyboxListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onChannelList(List<Channel> channels){

            }

            @Override
            public void onChannelJoined(String channel) {
                channelJoined(channel);
            }

            @Override
            public void onChannelLeft(String channel) {

            }

            @Override
            public void onAlreadyLoggedIn() {

            }
        });
    }

    public void channelJoined(String channel){
        Log.i(LOG_TAG, "Joined channel " + channel);
        ChannelFragment f = new ChannelFragment();
        f.setChannelName(channel);
        addFragment(f);
        showFragment(f);
    }

    public void showFragment(int i){
        showFragment(this.fragments.get(i));
    }

    public void showFragment(Fragment f){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, f);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private ChannelFragment getFragment(String label){
        for(ChannelFragment f : fragments){
            if(f.getChannelName().equals(label)){
                return f;
            }
        }

        return null;
    }

    private void removeFragment(String label){
        ChannelFragment f = getFragment(label);
        if(f != null){
            fragments.remove(f);
        }
    }

    private void removeFragment(int index){
        this.fragments.remove(index);
    }

    private void addFragment(ChannelFragment f){
        this.fragments.add(f);
    }

    private void showLoginDialog() {
        loginDialog = new Dialog(context);
        loginDialog.setContentView(R.layout.dialog_login);
        loginDialog.setCanceledOnTouchOutside(false);

        Button dialogButton = (Button) loginDialog.findViewById(R.id.buttonLogin);

        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText dialogUser = (EditText) loginDialog.findViewById(R.id.editUsername);
                EditText dialogPass = (EditText) loginDialog.findViewById(R.id.editPassword);

                String user = dialogUser.getText().toString().trim();
                String pass = dialogPass.getText().toString().trim();

                new LoginTask(new LoginTask.LoginTaskListener(){
                    @Override
                    public void onSuccess(String sid, String id, String token){
                        loginDialog.dismiss();
                        initializeChatbox(sid,id,token);
                    }

                    @Override
                    public void onError(String error){
                        Toast.makeText(PonyboxActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }).execute(user,pass);
            }
        });

        loginDialog.show();
    }

    public void initializeChatbox(String sid, String token, String id){
        client.setUser(sid,token,id);
        client.loadChatbox();
        Toast toast = Toast.makeText(context, "Connecté à la Chatbox", Toast.LENGTH_SHORT);
        toast.show();
    }
}
