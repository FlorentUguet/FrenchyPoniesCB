package fr.fusoft.frenchyponiescb.ponybox;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Florent on 24/03/2017.
 */

public class Channel {
    private String name;
    private String label;
    private boolean locked;
    private String description;

    private String LOG_TAG = "Channel";

    private List<User> users = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();

    private Ponybox ponybox;

    private ChannelListener mListener = null;

    public interface ChannelListener{
        void onMessageListUpdated(List<Message> messages);
        void onUserListUpdated(List<User> users);
        void onMessageAdded(Message m);
    }

    public Channel(Ponybox parent, String input){
        setParent(parent);
        try {
            loadMessage(new JSONObject(input));
        }catch(Exception e){
            Log.e(LOG_TAG,"Error loading channel : " + e.toString());
        }
    }

    public Channel(Ponybox parent, JSONObject oJson){
        setParent(parent);
        loadMessage(oJson);
    }

    public void setListener(ChannelListener listener){
        this.mListener = listener;
    }

    public void setParent(Ponybox parent){
        this.ponybox = parent;
    }

    public void loadMessage(JSONObject oJson){
       try {
           name = oJson.getString("name");
           label = oJson.getString("label");
           locked = oJson.getBoolean("locked");
           description = oJson.getString("description");
       }catch(Exception e){
           Log.e(LOG_TAG,"Error loading channel : " + e.toString());
       }
    }

    public void sendMessage(String message, User to){
        sendMessage(message, to.getUsername());
    }

    public void sendMessage(String message, String to){
        ponybox.sendMessage(this.name, message, to);
    }

    public void setUsers(List<User> users){
        this.users = users;

        if(this.mListener != null)
            this.mListener.onUserListUpdated(users);
    }

    public void addMessage(Message m){
        this.messages.add(m);

        if(this.mListener != null)
            this.mListener.onMessageAdded(m);
    }

    public void addMessages(List<Message> messages){
        this.messages.addAll(messages);
    }

    public List<User> getUsers(){
        return this.users;
    }

    public List<Message> getMessages(){
        return this.messages;
    }

    public String getName()
    {
        return this.name;
    }

    public String getLabel()
    {
        return this.label;
    }
}
