package fr.fusoft.frenchyponiescb.views;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.fusoft.frenchyponiescb.PonyboxApplication;
import fr.fusoft.frenchyponiescb.R;
import fr.fusoft.frenchyponiescb.adapters.MessageAdapter;
import fr.fusoft.frenchyponiescb.adapters.UserListAdapter;
import fr.fusoft.frenchyponiescb.ponybox.Channel;
import fr.fusoft.frenchyponiescb.ponybox.Message;
import fr.fusoft.frenchyponiescb.ponybox.User;

/**
 * Created by fuguet on 06/01/18.
 */

public class ChannelFragment extends Fragment {
    private View view;
    private ListView lvMessages;
    private ListView lvUsers;

    private MessageAdapter messageAdapter;
    private UserListAdapter userAdapter;

    protected int lastPosition = -1;

    private String channelName;
    private Channel channel = null;
    private final String LOG_TAG = "ChannelFragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        //ListView Messages
        this.lvMessages = (ListView) view.findViewById(R.id.listViewMessages);
        this.lvUsers = (ListView) view.findViewById(R.id.listViewUsers);

        lvMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String s = messageAdapter.getItem(i).getSender().getUsername();
                TextView txt = (TextView) getActivity().findViewById(R.id.editTextMessage);
                txt.setHint("Message");
            }
        });

        final EditText t = (EditText)view.findViewById(R.id.editTextMessage);
        Button b = (Button)view.findViewById(R.id.buttonSend);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(channel != null){
                    String message = t.getText().toString();
                    channel.sendMessage(message, "");
                    t.getText().clear();
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.loadChannel(this.channelName);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's state here

        if(this.channel != null)
            outState.putString("channel", this.channel.getName());
    }

    private void loadChannel(String channelName){
        PonyboxApplication app = (PonyboxApplication) getActivity().getApplication();
        final Channel c = app.getClient().getChannel(channelName);

        if(c != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {loadChannel(c);}
            });

        }else{
            Log.w(LOG_TAG, "Channel " + channel + " was not found in the FClient");
        }
    }

    private void loadChannel(Channel channel){
        this.channel = channel;

        if(this.messageAdapter == null){
            this.initMessageAdapter();
        }

        if(this.userAdapter == null){
            this.initUserAdapter();
        }

        this.initMessageListView(channel.getMessages());
        this.initUserListView(channel.getUsers());

        this.channel.setListener(new Channel.ChannelListener() {
            @Override
            public void onMessageListUpdated(List<Message> messages) {

            }

            @Override
            public void onMessageAdded(Message m){
                addEntry(m);
            }

            @Override
            public void onUserListUpdated(List<User> users) {
                updateUsers(users);
            }
        });

    }

    public void addEntry(final Message entry){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(messageAdapter == null){updateMessages();}
                else{
                    addMessage(entry);
                }
            }
        });
    }

    public void updateMessages(){
        updateMessages(channel.getMessages());
    }

    public void updateMessages(final List<Message> entries){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setMessages(entries);
            }
        });
    }

    public void updateUsers() {
        updateUsers(channel.getUsers());
    }

    public void updateUsers(final List<User> users){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userAdapter.clear();
                userAdapter.addAll(users);
                userAdapter.notifyDataSetChanged();
            }
        });
    }

    protected void initMessageAdapter(){
        messageAdapter = new MessageAdapter(new ArrayList<Message>(),getActivity());
    }

    public void initUserAdapter(){
        userAdapter = new UserListAdapter(new ArrayList<User>(),  getActivity());
    }

    protected void initMessageListView(final List<Message> entries){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lvMessages.setAdapter(messageAdapter);
                setMessages(entries);

                if(ChannelFragment.this.lastPosition >= 0){
                    lvMessages.setSelection(ChannelFragment.this.lastPosition);
                }
            }
        });
    }


    protected void initUserListView(final List<User> users){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lvUsers.setAdapter(userAdapter);
                updateUsers(users);
            }
        });
    }

    protected void setMessages(final List<Message> messages){
        if(this.messageAdapter != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageAdapter.clear();
                    messageAdapter.addAll(messages);
                }
            });
        }
    }

    protected void addMessage(final Message message){
        if(messageAdapter != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageAdapter.add(message);
                }
            });
        }
    }

    public void setChannelName(String channel){
        this.channelName = channel;
    }

    public String getChannelName(){
        return this.channelName;
    }

}
