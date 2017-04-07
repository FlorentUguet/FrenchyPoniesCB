package fr.fusoft.frenchyponiescb;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
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

import fr.fusoft.frenchyponiescb.adapters.MessageAdapter;
import fr.fusoft.frenchyponiescb.events.AskFullMessageListEvent;
import fr.fusoft.frenchyponiescb.events.ChannelJoinedEvent;
import fr.fusoft.frenchyponiescb.events.MessageRecievedEvent;
import fr.fusoft.frenchyponiescb.ponybox.Channel;
import fr.fusoft.frenchyponiescb.ponybox.Message;
import fr.fusoft.frenchyponiescb.ponybox.PonyboxClient;
import fr.fusoft.frenchyponiescb.ponybox.User;

public class ChatActivity extends AppCompatActivity {

    final Context context = this;
    Dialog loginDialog;

    User currentUser;

    PonyboxClient client = PonyboxClient.Client;

    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    private TabLayout tabLayout;

    String LOG_TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.pagerChannels);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Button buttonSend = (Button) findViewById(R.id.buttonSendMessage);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        /*
        client.setListener(new PonyboxClient.PonyboxListener() {
            @Override
            public void onChannelJoined(Channel c) {
                Log.i(LOG_TAG,"Channel " + c.GetLabel() + " joined in Activity");
                ChannelJoined(c);
            }

            public void onPrivateMessageRecieved(Message m){

            }

            public void onPrivateMessageListRecieved(List<Message> m){

            }

        });
        */

        ShowLoginDialog();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        Log.e(LOG_TAG,"onResume");

        for(int i=0;i<pagerAdapter.getCount();i++){
            EventBus.getDefault().post(new AskFullMessageListEvent(((ChatActivityFragment)pagerAdapter.getItem(i)).channel));
        }

    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        Log.e(LOG_TAG,"onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void ShowLoginDialog()
    {
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

                new LoginTask().execute(user,pass);
            }
        });

        loginDialog.show();
    }

    public void InitializeChatbox(String sid, String token, String id)
    {
        client.SetUser(sid,token,id);
        client.LoadChatbox();
        Toast toast = Toast.makeText(context, "Connecté à la Chatbox", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void ChannelJoined(Channel channel) {
        final Channel c = channel;

        for(int i=0;i<pagerAdapter.getCount();i++){
            ChatActivityFragment f = (ChatActivityFragment) pagerAdapter.getItem(i);
            if(f.getChannel().equals(c.GetName())){
                f = null;
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bundle arguments = new Bundle();
                arguments.putString("channel", c.GetName());

                Fragment f = new ChatActivityFragment();
                f.setArguments(arguments);

                pagerAdapter.addFragment(f,c.GetLabel());
                pagerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Subscribe
    public void onChannelJoinedEvent(ChannelJoinedEvent event){
        Log.d(LOG_TAG,"EVENT : Joined channel " + event.channel.GetName());
        ChannelJoined(event.channel);
    }


    public void PrivateMessageRecieved(Message message)
    {
        final Message m = message;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String u;

                if(currentUser.GetUsername().equals(m.GetSender().GetUsername())){
                    u = m.GetRecipient().GetUsername();
                }else{
                    u = m.GetSender().GetUsername();
                }

                Bundle arguments = new Bundle();
                arguments.putString("channel", m.GetChannel());
                arguments.putString("to", u);

                Fragment f = new ChatActivityFragment();
                f.setArguments(arguments);

                pagerAdapter.addFragment(f,u);
                pagerAdapter.notifyDataSetChanged();
            }
        });
    }

    public void SendMessage(){
        int index = viewPager.getCurrentItem();
        ChatActivityFragment f = (ChatActivityFragment) pagerAdapter.getItem(index);
        EditText input = (EditText) findViewById(R.id.editTextMessage);
        client.SendMessage(f.getChannel(),input.getText().toString(),f.getTo());
        input.setText("");
    }

    private void setupViewPager(ViewPager viewPager) {
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        private final String COOKIES_HEADER = "Set-Cookie";
        private CookieManager msCookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

        String sid;
        String id;
        String token;
        private String cb_login = "http://www.frenchy-ponies.fr/ucp.php?mode=login";
        private String cb_pb_include = "http://frenchy-ponies.fr/ponybox/pb-include.php";

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
                if(matcher.matches())
                {
                    id = matcher.group(1);
                }
                else
                {
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
                loginDialog.dismiss();
                Toast toast = Toast.makeText(context, "Connecté à la Frenchy Ponies", Toast.LENGTH_SHORT);
                toast.show();
                InitializeChatbox(sid, token, id);
            }else{
                TextView error = (TextView) loginDialog.findViewById(R.id.textViewError);
                error.setText("Données saisies incorrectes");
            }
        }

        private String getQuery(List<Pair<String, String>> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;



            for (Pair<String,String> pair : params)
            {
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


}
