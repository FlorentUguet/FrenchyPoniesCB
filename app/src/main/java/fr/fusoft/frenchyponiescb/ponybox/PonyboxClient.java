package fr.fusoft.frenchyponiescb.ponybox;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import fr.fusoft.frenchyponiescb.R;
import fr.fusoft.frenchyponiescb.events.AskFullMessageListEvent;
import fr.fusoft.frenchyponiescb.events.ChannelJoinedEvent;
import fr.fusoft.frenchyponiescb.events.FullMessageListEvent;
import fr.fusoft.frenchyponiescb.events.MessageListUpdatedEvent;
import fr.fusoft.frenchyponiescb.events.MessageRecievedEvent;
import fr.fusoft.frenchyponiescb.events.SendMessageEvent;
import fr.fusoft.frenchyponiescb.events.UserListUpdatedEvent;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Florent on 22/03/2017.
 */

public class PonyboxClient extends IntentService {

    public static final PonyboxClient Client = new PonyboxClient();

    public interface PonyboxListener {
        void onChannelJoined(Channel c);
        void onPrivateMessageRecieved(Message m);
        void onPrivateMessageListRecieved(List<Message> m);
    }

    public interface PonyboxPrivateTabListener {
        void onPublicMessageRecieved(Message m);
        void onPublicMessageListRecieved(List<Message> m);

    }

    public interface PonyboxTabListener {
        void onPublicMessageRecieved(Message m);
        void onPublicMessageListRecieved(List<Message> m);
    }

    private String cb_address = "http://94.23.60.187:8080";

    private Context mContext;

    private String token = "";
    private String sid = "";
    private String id = "";

    NotificationManagerCompat notificationManager;

    private static String LOG_TAG = "PonyboxClient";

    private Socket socket;

    private PonyboxListener listener;
    private HashMap<String,PonyboxTabListener> tabListeners = new HashMap<>();
    private HashMap<String,PonyboxPrivateTabListener> privateTabListeners = new HashMap<>();

    HashMap<String,List<Message>> messages = new HashMap<>();

    public PonyboxClient() {
        super(LOG_TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");

        mContext = getApplicationContext();

        SetUser(intent.getStringExtra("sid"),intent.getStringExtra("token"),intent.getStringExtra("id"));
        LoadChatbox();
        EventBus.getDefault().register(this);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");
    }

    public void SetUser(String sid, String token, String id){
        this.sid = sid;
        this.id = id;
        this.token = token;
    }

    public boolean LoadChatbox(){
        try {
            socket = IO.socket(cb_address);

            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on("login", onLogin);
            socket.on("already-logged", onAlreadyLogged);
            socket.on("login-success", onLoginSuccess);
            socket.on("join-channel", onJoinChannel);
            socket.on("new-message", onNewMessage);
            socket.on("channel-messages", onChannelMessages);
            socket.on("refresh-new-channels", onRefreshNewChannels);
            socket.on("get-older-message", onGetOlderMessage);
            socket.on("refresh-channel-users", onRefreshChannelUsers);

            socket.connect();

        }catch(Exception e){
            Log.e(LOG_TAG,"Error loading Socket.IO client : " + e.toString());
        }
        return true;
    }

    @Subscribe
    public void onSendMessageEvent(SendMessageEvent event){
        SendMessage(event.channel,event.message,event.to);
    }

    @Subscribe
    public void onAskFullMessageListEvent(AskFullMessageListEvent event){
        EventBus.getDefault().post(new FullMessageListEvent(event.channel, messages.get(event.channel)));
    }

    public void GetOlderMessages(String channel)    {
        if(messages.containsKey(channel))
            if(messages.get(channel).size() > 0)
                socket.emit("get-older-messages", channel, messages.get(channel).get(0).GetId());
    }

    public void SendMessage(String channel, String message, String to)    {
        if(to != null)
            if(to.equals(""))
                to = null;

        socket.emit("send-message", channel, message, to, true, new Ack() {
            @Override
            public void call(Object... args)
            {
                Log.i(LOG_TAG,"Message sent");
            }
        });
    }

    public List<Message> GetMessages(String channel)
    {
        return messages.get(channel);
    }

    public void setListener(PonyboxListener listener)    {
        this.listener = listener;
    }

    public void setTabListener(String channel, PonyboxTabListener listener)    {
        this.tabListeners.put(channel,listener);
        tabListeners.get(channel).onPublicMessageListRecieved(messages.get(channel));
    }

    public static List<Channel> LoadChannels(JSONObject root) {
        List<Channel> c = new ArrayList<>();

        return c;
    }

    public static List<Message> LoadMessages(JSONArray root) {
        List<Message> m = new ArrayList<>();

        try {
            for (int i = 0; i < root.length(); i++) {
                m.add(new Message(root.getJSONObject(i)));
            }
        }catch(Exception e)
        {
            Log.e(LOG_TAG,"Error loading older messages : " + e.toString());
        }

        return m;
    }

    private void ShowMessageNotification(String text)
    {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("New Message")
                        .setContentText(text);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Builds the notification and issues it.
        notificationManager.notify(null,0 , mBuilder.build());
    }

    private  Ack createAck = new Ack() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG,"Recieved ack from creation");
            socket.emit("login");
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener()     {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG,"Connected to the Chatbox");
            socket.emit("create",Integer.parseInt(id),token,createAck);
        }
    };

    private Emitter.Listener onAlreadyLogged = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG,"Already logged in");
        }
    };

    private Emitter.Listener onLoginSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG,"Logged in successfully");

            //TODO : Remove this test message
            //SendMessage("general","Très le test Android","");
        }
    };

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG,"Logged in");
        }
    };

    private Emitter.Listener onJoinChannel = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Channel c = new Channel((JSONObject) args[0]);
            Log.i(LOG_TAG,"Channel " + c.GetLabel() + " joined");
            messages.put(c.GetName(),new ArrayList<Message>());
            EventBus.getDefault().post(new ChannelJoinedEvent(c));
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message m = new Message ((JSONObject) args[0]);

            messages.get(m.GetChannel()).add(m);

            //ShowMessageNotification("New messages from " + m.GetChannel());

            Log.i(LOG_TAG,"Message recieved");
            EventBus.getDefault().post(new MessageRecievedEvent(m));
        }
    };

    private Emitter.Listener onRefreshChannelUsers = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                String channel = (String) args[0];
                JSONArray aUsers = (JSONArray) args[1];
                List<User> users = new ArrayList<>();

                for(int i=0;i<aUsers.length();i++){
                    users.add(new User(aUsers.getJSONObject(i)));
                }

                Collections.sort(users);

                EventBus.getDefault().post(new UserListUpdatedEvent(channel,users));
            }catch(Exception e){
                Log.e(LOG_TAG,"Error recieving User List : " + e.toString());
            }
        }
    };

    private Emitter.Listener onChannelMessages = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG,"Channel messages");
        }
    };

    private Emitter.Listener onRefreshNewChannels = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            List<Channel> c = LoadChannels((JSONObject)args[0]);
            Log.i(LOG_TAG,"Loaded " + c.size() + " channels");
        }
    };

    private Emitter.Listener onGetOlderMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String channel = (String)args[0];
            List<Message> m = LoadMessages((JSONArray)args[1]);

            messages.get(channel).addAll(m);

            Collections.sort(messages.get(channel));

            EventBus.getDefault().post(new MessageListUpdatedEvent(channel,messages.get(channel)));

            Log.i(LOG_TAG,"Got " + m.size() + " older messages for channel " + channel);
        }
    };
}
