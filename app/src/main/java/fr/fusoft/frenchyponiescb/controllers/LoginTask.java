package fr.fusoft.frenchyponiescb.controllers;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.fusoft.frenchyponiescb.R;

/**
 * Created by fuguet on 06/01/18.
 */

public class LoginTask extends AsyncTask<String, Void, Boolean> {
    public interface LoginTaskListener{
        void onSuccess(String sid, String id, String token);
        void onError(String error);
    }

    private final String LOG_TAG = "LoginTask";
    private final String COOKIES_HEADER = "Set-Cookie";
    private CookieManager msCookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    private String sid;
    private String id;
    private String token;
    private String cb_login = "http://www.frenchy-ponies.fr/ucp.php?mode=login";
    private String cb_pb_include = "http://frenchy-ponies.fr/ponybox/pb-include.php";

    public LoginTaskListener mListener;

    public LoginTask(LoginTaskListener listener){
        this.mListener = listener;
    }

    protected Boolean doInBackground(String... input) {
        String user = input[0];
        String pass = input[1];

        CookieHandler.setDefault(msCookieManager);

        HttpURLConnection conn;
        try {
            //Login
            int status;
            URL url = new URL(cb_login);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            List<Pair<String, String>> params = new ArrayList<>();
            params.add(new Pair<>("username",user));
            params.add(new Pair<>("password",pass));
            params.add(new Pair<>("redirect","index.php"));
            params.add(new Pair<>("login","Connexion"));
            //params.add(new Pair<>("sid","c8bbb4e8ad558695d4796ca7c340c881"));
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(params));
            writer.flush();
            writer.close();
            os.close();

            //Cookies
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }

            //Ponybox-Include
            url = new URL(cb_pb_include);
            conn = (HttpURLConnection) url.openConnection();

            //Cookies
            List<HttpCookie> c = msCookieManager.getCookieStore().getCookies();

            Log.d(LOG_TAG, msCookieManager.getCookieStore().getCookies().size() + " Cookies to load");


            if(msCookieManager.getCookieStore().getCookies().size()>0) {
                conn.setRequestProperty("Cookie", TextUtils.join(",",  msCookieManager.getCookieStore().getCookies()));
            }

            status = conn.getResponseCode();

            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            if(result.toString().equals("")){
                Log.w(LOG_TAG,"Invalid credentials");
                return false;
            }

            //Parsing

            JSONObject obj = new JSONObject(result.toString());

            token = obj.getString("token");
            sid = obj.getString("sid");

            Pattern pattern = Pattern.compile("^user([0-9]+)\\.pony.*");
            Matcher matcher = pattern.matcher(sid);
            if(matcher.matches())            {
                id = matcher.group(1);
            }
            else            {
                Log.e(LOG_TAG,"ID not found while matching the sid");
                return false;
            }

            Log.i(LOG_TAG,"Logged in with sid (" + token + "), token (" + sid + "), uid(" + id + ")");

            return true;

        }catch(Exception e){
            Log.e(LOG_TAG,"Error querying the login page : " + e.toString());
        }

        return false;
    }

    protected void onPostExecute(Boolean ok) {
        if(ok){
            this.mListener.onSuccess(sid, token, id);
        }else{
            this.mListener.onError("Données saisies incorrectes");
        }
    }

    private String getQuery(List<Pair<String, String>> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Pair<String,String> pair : params) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.first, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.second, "UTF-8"));
        }

        return result.toString();
    }

}
