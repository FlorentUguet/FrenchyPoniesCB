package fr.fusoft.frenchyponiescb.ponybox;

import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Florent on 22/03/2017.
 *
 */

public class Message implements Serializable, Comparable {
    long id;
    String format;
    int type;
    Boolean isPrivate;
    User from;
    User to;
    String channel;
    long sendDate;
    HashMap<String, Boolean> rights = new HashMap<>();

    String LOG_TAG = "Message";

    public Message(String input)
    {
        try {
            loadMessage(new JSONObject(input));
        }catch(Exception e){
            Log.e(LOG_TAG,"Error loading Message " + e.getMessage());
        }
    }

    public Message(JSONObject oJson)
    {
        loadMessage(oJson);
    }

    public void loadMessage(JSONObject oJson)
    {
        try {
            id = oJson.getLong("id");
            format = oJson.getString("format");
            type = oJson.getInt("type");
            sendDate = oJson.getLong("sendDate");
            //isPrivate = oJson.private;

            //from
            from = new User(oJson.getJSONObject("from"));

            //to
            if (oJson.has("to"))
                if(!oJson.isNull("to"))
                    to = new User(oJson.getJSONObject("to"));

            channel = oJson.getString("channel");
            isPrivate = oJson.getBoolean("private");

        }catch(Exception e){
            Log.e(LOG_TAG,"Error loading Message " + e.getMessage());
        }
    }

    public long GetTimestamp()
    {
        return this.sendDate;
    }

    public String GetSendDate() {
        Date d =  new Date(this.sendDate);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return  df.format(d);
    }

    public String GetMessage()
    {
        return this.format;
    }

    public String GetChannel()
    {
        return this.channel;
    }

    public User GetSender()
    {
        return this.from;
    }

    public User GetRecipient()
    {
        return this.to;
    }

    public boolean isPrivate()
    {
        return this.isPrivate;
    }

    public long GetId()
    {
        return this.id;
    }


    public int compareTo(Object o) {
        if (!(o instanceof Message))
            throw new ClassCastException();

        Message e = (Message) o;

        //User
        return ((Long)sendDate).compareTo(e.GetTimestamp());
    }
}
