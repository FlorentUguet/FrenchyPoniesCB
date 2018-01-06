package fr.fusoft.frenchyponiescb.ponybox;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by fuguet on 05/01/18.
 */

public class Ponybox {
    private final static String LOG_TAG = "PonyBox";

    private Socket socket;

    private String token = "";
    private String sid = "";
    private String id = "";

    public interface PonyboxListener{
        void onConnected();
        void onDisconnected();
        void onChannelJoined(String channel);
        void onChannelLeft(String channel);
        void onAlreadyLoggedIn();
        void onChannelList(List<Channel> channels);
    }

    private List<Channel> channelList;
    private Map<String, Channel> channels = new HashMap<>();
    private String url = "";
    private PonyboxListener mListener = null;

    public Ponybox(String url){
        this.url = url;
    }

    public void setListener(PonyboxListener listener){
        this.mListener = listener;
    }

    public static List<Message> loadMessages(JSONArray root) {
        List<Message> m = new ArrayList<>();

        try {
            for (int i = 0; i < root.length(); i++)
                m.add(new Message(root.getJSONObject(i)));
        }catch(Exception e) {
            Log.e(LOG_TAG,"Error loading older messages : " + e.toString());
        }

        return m;
    }

    public Channel getChannel(String channelName){
        if(this.channels.containsKey(channelName))
            return this.channels.get(channelName);
        else
            return null;
    }

    public void setUser(String sid, String token, String id){
        this.sid = sid;
        this.id = id;
        this.token = token;
    }

    public boolean loadChatbox(){
        try {
            socket = IO.socket(this.url);

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
            return false;
        }
        return true;
    }

    public List<Channel> getChannelList(){
        return this.channelList;
    }

    public void sendMessage(String channel, String message, String to){
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

    private Ack createAck = new Ack() {
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

            if(mListener != null)
                mListener.onAlreadyLoggedIn();
        }
    };

    private Emitter.Listener onLoginSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(LOG_TAG,"Logged in successfully");

            if(mListener != null)
                mListener.onConnected();
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
            Channel c = new Channel(Ponybox.this, (JSONObject) args[0]);
            Log.i(LOG_TAG,"Channel " + c.getName() + " joined");
            channels.put(c.getName(), c);

            if(mListener != null)
                mListener.onChannelJoined(c.getName());
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Message m = new Message((JSONObject) args[0]);
            channels.get(m.getChannel()).addMessage(m);
            Log.i(LOG_TAG, "New message for channel " + m.channel);
        }
    };

    private Emitter.Listener onRefreshChannelUsers = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                String channel = (String) args[0];
                JSONArray aUsers = (JSONArray) args[1];
                List<User> users = new ArrayList<>();
                for(int i=0;i<aUsers.length();i++){users.add(new User(aUsers.getJSONObject(i)));}
                Collections.sort(users);
                channels.get(channel).setUsers(users);
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

    public static List<Channel> loadChannels(JSONObject root) {
        List<Channel> c = new ArrayList<>();

        return c;
    }

    private Emitter.Listener onRefreshNewChannels = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            List<Channel> c = loadChannels((JSONObject)args[0]);
            channelList = c;

            if(mListener != null)
                mListener.onChannelList(c);
        }
    };

    private Emitter.Listener onGetOlderMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String channel = (String)args[0];
            List<Message> m = loadMessages((JSONArray)args[1]);
            channels.get(channel).addMessages(m);

            Log.i(LOG_TAG,"Got " + m.size() + " older messages for channel " + channel);
        }
    };
}
