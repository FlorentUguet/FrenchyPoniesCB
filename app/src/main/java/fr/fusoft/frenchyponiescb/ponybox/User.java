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
    private long uid;
    private String LOG_TAG = "User";
    private String username;
    private String color;
    private String avatarUrl;
    private String avatar;
    private boolean isActive;
    private HashMap<String, Boolean> rights = new HashMap<>();
    private Drawable avatarDrawable;

    public User(String input){
        try {
            loadUser(new JSONObject(input));
        }catch(Exception e){
            Log.e(LOG_TAG,"Error loading user " + e.getMessage());
        }
    }

    public User(JSONObject oJson){
        loadUser(oJson);
    }

    public void loadUser(JSONObject oJson){
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

    public HashMap<String,Boolean> getRights(){return this.rights;}
    public String getUsername()
    {
        return this.username;
    }
    public String getColor()
    {
        return this.color;
    }
    public String getAvatarUrl(){return this.avatarUrl;}
    public boolean isActive(){return this.isActive;}

    public int compareTo(Object o) {
        if (!(o instanceof User))
            throw new ClassCastException();

        User e = (User) o;

        //Admin
        if(rights.get("admin") != e.getRights().get("admin"))
            return -(rights.get("admin").compareTo(e.getRights().get("admin")));

        //Modo
        if(rights.get("modo") != e.getRights().get("modo"))
            return -(rights.get("modo").compareTo(e.getRights().get("modo")));

        //User
        return username.compareTo(e.getUsername());
    }

    public void loadAvatar(ImageView iv, Context c)
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
