package fr.fusoft.frenchyponiescb.ponybox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONObject;

import java.util.HashMap;

import fr.fusoft.frenchyponiescb.FPUtils;

/**
 * Created by Florent on 22/03/2017.
 */

public class User implements Comparable {
    long uid;
    private String LOG_TAG = "User";
    String username;
    String color;
    String avatarUrl;
    String avatar;
    boolean isActive;
    HashMap<String, Boolean> rights = new HashMap<>();

    Drawable avatarDrawable;

    public User(String input)
    {
        try {
            loadUser(new JSONObject(input));
        }catch(Exception e){
            Log.e(LOG_TAG,"Error loading user " + e.getMessage());
        }
    }

    public User(JSONObject oJson)
    {
        loadUser(oJson);
    }

    public void loadUser(JSONObject oJson)    {
        try {
            uid = oJson.getLong("uid");
            username = oJson.getString("username");
            color = oJson.getString("color");
            avatar = oJson.getString("avatar");
            avatarUrl = "http://frenchy-ponies.fr/download/file.php?avatar=" + oJson.getString("avatar");

            if(oJson.has("isActive"))
                this.isActive = oJson.getBoolean("isActive");

            JSONObject oRights = oJson.getJSONObject("rights");

            //Rights
            rights.put("admin", oRights.getBoolean("admin"));
            rights.put("modo", oRights.getBoolean("modo"));
            rights.put("hot", oRights.getBoolean("hot"));
            rights.put("quizz", oRights.getBoolean("quizz"));
            rights.put("edit", oRights.getBoolean("edit"));
            rights.put("delete", oRights.getBoolean("delete"));
        }catch(Exception e){
            Log.e(LOG_TAG,"Error loading User " + e.getMessage());
        }
    }

    public String toString(){
        return username + "/" + uid;
    }

    public HashMap<String,Boolean> GetRights(){return this.rights;}
    public String GetUsername()
    {
        return this.username;
    }
    public String GetColor()
    {
        return this.color;
    }
    public String GetAvatarUrl(){return this.avatarUrl;}
    public boolean isActive(){return this.isActive;}

    public int compareTo(Object o) {
        if (!(o instanceof User))
            throw new ClassCastException();

        User e = (User) o;

        //Admin
        if(rights.get("admin") != e.GetRights().get("admin"))
            return -(rights.get("admin").compareTo(e.GetRights().get("admin")));

        //Modo
        if(rights.get("modo") != e.GetRights().get("modo"))
            return -(rights.get("modo").compareTo(e.GetRights().get("modo")));

        //User
        return username.compareTo(e.GetUsername());
    }

    public void LoadAvatar(ImageView iv, Context c)
    {
        new LoadImageTask(iv, c).execute();
    }

    public class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        ImageView bmImage;
        Context context;

        public LoadImageTask(ImageView bmImage, Context c) {
            this.bmImage = bmImage;
            this.context = c;
        }

        protected Bitmap doInBackground(Void... urls) {
            String urlDisplay = avatarUrl;

            Drawable d = FPUtils.loadCachedDrawable(context,avatar);

            if(d == null){
                d = FPUtils.loadDrawableFromUrl(urlDisplay);
                FPUtils.cacheDrawable(context,d,avatar);
            }

            return FPUtils.drawableToBitmap(d);
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
