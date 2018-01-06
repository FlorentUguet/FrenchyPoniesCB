package fr.fusoft.frenchyponiescb.views;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import fr.fusoft.frenchyponiescb.R;
import fr.fusoft.frenchyponiescb.adapters.MessageAdapter;
import fr.fusoft.frenchyponiescb.ponybox.Message;

/**
 * Created by fuguet on 06/01/18.
 */

public class ChannelFragment extends Fragment {
    private View view;
    private ListView lv;
    private MessageAdapter adapter;
    private String channel;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        //ListView Messages
        lv = (ListView) view.findViewById(R.id.listViewMessages);
        adapter = new MessageAdapter(new ArrayList<Message>(), getActivity());
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String s = adapter.getItem(i).getSender().getUsername();
                TextView txt = (TextView) getActivity().findViewById(R.id.editTextMessage);
                txt.setHint("Message");
            }
        });

        return view;
    }

    public void setChannelName(String channel){
        this.channel = channel;
    }

    public String getChannelName(){
        return this.channel;
    }

}
