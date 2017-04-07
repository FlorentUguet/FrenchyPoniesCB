package fr.fusoft.frenchyponiescb.ponybox;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by Florent on 24/03/2017.
 */

public class Channel {
    String name;
    String label;
    boolean locked;
    String description;

    String LOG_TAG = "Channel";

    public Channel(String input)
    {
        try {
            loadMessage(new JSONObject(input));
        }catch(Exception e){
            Log.e(LOG_TAG,"Error loading channel : " + e.toString());
        }

    }

    public Channel(JSONObject oJson)
    {
        loadMessage(oJson);
    }

    public void loadMessage(JSONObject oJson)
    {
       try {
           name = oJson.getString("name");
           label = oJson.getString("label");
           locked = oJson.getBoolean("locked");
           description = oJson.getString("description");
       }catch(Exception e){
           Log.e(LOG_TAG,"Error loading channel : " + e.toString());
       }
    }

    public String GetName()
    {
        return this.name;
    }

    public String GetLabel()
    {
        return this.label;
    }
}
