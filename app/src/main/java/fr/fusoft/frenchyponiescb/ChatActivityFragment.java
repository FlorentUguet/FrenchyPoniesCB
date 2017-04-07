package fr.fusoft.frenchyponiescb;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import fr.fusoft.frenchyponiescb.adapters.MessageAdapter;
import fr.fusoft.frenchyponiescb.adapters.UserListAdapter;
import fr.fusoft.frenchyponiescb.events.AskFullMessageListEvent;
import fr.fusoft.frenchyponiescb.events.AskOlderMessagesEvent;
import fr.fusoft.frenchyponiescb.events.FullMessageListEvent;
import fr.fusoft.frenchyponiescb.events.MessageListUpdatedEvent;
import fr.fusoft.frenchyponiescb.events.MessageRecievedEvent;
import fr.fusoft.frenchyponiescb.events.UserListUpdatedEvent;
import fr.fusoft.frenchyponiescb.ponybox.Message;
import fr.fusoft.frenchyponiescb.ponybox.PonyboxClient;
import fr.fusoft.frenchyponiescb.ponybox.User;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChatActivityFragment extends Fragment {
    String channel = "";
    String to = "";
    PonyboxClient client = PonyboxClient.Client;
    MessageAdapter adapter;
    String LOG_TAG = "ChatActivityFragment";
    UserListAdapter userListAdapter;
    ListView lvUserList;
    SwipeRefreshLayout swipeRefreshLayout;

    ListView lv;

    View view;

    public ChatActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();

        if(arguments.containsKey("channel"))
            this.channel = arguments.getString("channel");

        if(arguments.containsKey("to"))
            this.to = arguments.getString("to");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if(arguments.containsKey("channel"))
            this.channel = arguments.getString("channel");

        if(arguments.containsKey("to"))
            this.to = arguments.getString("to");

        view = inflater.inflate(R.layout.fragment_chat, container, false);

        //Swipe Refresh
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                client.GetOlderMessages(getChannel());
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //ListView Messages
        lv = (ListView) view.findViewById(R.id.listViewMessages);
        adapter = new MessageAdapter(new ArrayList<Message>(), getContext());
        lv.setAdapter(adapter);

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem == 0 && listIsAtTop()){
                    swipeRefreshLayout.setEnabled(true);
                }else{
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String s = adapter.getItem(i).GetSender().GetUsername();

                TextView txt = (TextView) getActivity().findViewById(R.id.editTextMessage);

                if(to.equals(s))
                {
                    to = "";
                    txt.setHint("Message Public");
                }
                else
                {
                    to = s;
                    txt.setHint("MP : " + to);
                }
            }
        });

        //ListView users
        lvUserList = (ListView) view.findViewById(R.id.listViewUsers);
        userListAdapter = new UserListAdapter(new ArrayList<User>(), getContext());
        lvUserList.setAdapter(userListAdapter);

        lvUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String s = userListAdapter.getItem(i).GetUsername();

                TextView txt = (TextView) getActivity().findViewById(R.id.editTextMessage);

                if(to.equals(s))
                {
                    to = "";
                    txt.setHint("Message Public");
                }
                else
                {
                    to = s;
                    txt.setHint("MP : " + to);
                }
            }
        });

        Log.e(LOG_TAG,"onCreateView");

        return view;
    }

    private boolean listIsAtTop()   {
        if(lv.getChildCount() == 0) return true;
        return lv.getChildAt(0).getTop() == 0;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(LOG_TAG,"onStart");
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        Log.e(LOG_TAG,"onResume");

        //EventBus.getDefault().post(new AskFullMessageListEvent(this.channel));
        setMessages(client.GetMessages(this.channel));
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        Log.e(LOG_TAG,"onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        Log.e(LOG_TAG,"onStop");
        super.onStop();
    }

    @Subscribe
    public void onFullMessageListEvent(FullMessageListEvent event){
        Log.d(LOG_TAG, "Full message list recieved for channel " + event.channel);
        if(event.channel.equals(this.channel)){
            setMessages(event.messages);
        }
    }

    @Subscribe
    public void onMessageRecieved(MessageRecievedEvent event){
        Log.d(LOG_TAG,"EVENT : Message recieved " + event.message.GetSender().GetUsername());

        if(event.channel.equals(this.channel)){
            addMessage(event.message);
        }
    }

    @Subscribe
    public void onMessageRecieved(MessageListUpdatedEvent event){
        Log.d(LOG_TAG,"EVENT : Message List recieved " + event.channel);

        if(event.channel.equals(this.channel)){
            setMessages(event.messages);
        }
    }

    @Subscribe
    public void onUserListUpdated(UserListUpdatedEvent event){
        if(event.channel.equals(this.channel)){setUserlist(event.users);}
    }

    public void addMessage(Message message)
    {
        final Message m = message;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.addMessage(m);
                lv.invalidateViews();

                if(lv.getLastVisiblePosition() >= adapter.getCount() - 2){
                    lv.smoothScrollToPosition(adapter.getCount());
                }
            }
        });
    }

    public void setUserlist(List<User> users){
        final List<User> u = users;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userListAdapter.setUsers(u);
                lvUserList.invalidateViews();

                Log.d(LOG_TAG,"Userlist for " + channel + " has " + u.size() + " items (List)");
                Log.d(LOG_TAG,"Userlist for " + channel + " has " + userListAdapter.getCount() + " items (Adapter)");
            }
        });
    }

    public void setMessages(List<Message> messages)
    {
        final List<Message> m = messages;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.setMessages(m);
                lv.invalidateViews();

                if(lv.getLastVisiblePosition() >= adapter.getCount() - 2){
                    lv.smoothScrollToPosition(adapter.getCount());
                }

                Log.i(LOG_TAG, "MessageList => List : " + m.size() + " | LV : " + lv.getLastVisiblePosition() + " | AD : " + adapter.getCount());
            }
        });
    }

    public void addMessages(List<Message> messages)
    {
        final List<Message> m = messages;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.addMessages(m);
                lv.invalidateViews();
                //Log.i(LOG_TAG,"Adapter has " + adapter.getCount() + " items");

                if(lv.getLastVisiblePosition() >= adapter.getCount() - 2){
                    lv.smoothScrollToPosition(adapter.getCount());
                }
            }
        });
    }

    public String getChannel(){
        return this.channel;
    }
    public String getTo(){
        return this.to;
    }
}
