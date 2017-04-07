package fr.fusoft.frenchyponiescb.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fr.fusoft.frenchyponiescb.R;
import fr.fusoft.frenchyponiescb.ponybox.Message;
import fr.fusoft.frenchyponiescb.workers.URLImageParser;

/**
 * Created by Florent on 25/03/2017.
 */

public class MessageAdapter extends ArrayAdapter<Message> implements View.OnClickListener{

    private List<Message> messages;
    Context mContext;

    String LOG_TAG = "MessageAdapter";

    // View lookup cache
    private static class ViewHolder {
        TextView txtMessage;
        TextView txtUserTo;
        TextView txtUserFrom;
        TextView txtDate;
        ImageView avatar;
    }

    public MessageAdapter(List<Message> data, Context context) {
        super(context, R.layout.item_message, data);
        this.messages = data;
        this.mContext=context;

    }

    public void setMessages(List<Message> messages)
    {
        this.messages.clear();
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    public void addMessages(List<Message> messages)
    {
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    public void addMessage(Message message)
    {
        this.messages.add(message);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        Message message = (Message)object;

        switch (v.getId())
        {
            case R.id.imageViewAvatar:
                Snackbar.make(v, "User " + message.GetSender().GetUsername(), Snackbar.LENGTH_LONG).setAction("No action", null).show();
                break;
        }
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Message message = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_message, parent, false);
            viewHolder.txtDate = (TextView) convertView.findViewById(R.id.textViewDate);
            viewHolder.txtMessage = (TextView) convertView.findViewById(R.id.textViewMessage);
            viewHolder.txtUserFrom = (TextView) convertView.findViewById(R.id.textViewUserFrom);
            viewHolder.txtUserTo = (TextView) convertView.findViewById(R.id.textViewUserTo);
            viewHolder.avatar = (ImageView) convertView.findViewById(R.id.imageViewAvatar);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        lastPosition = position;

        try {
            message.GetSender().LoadAvatar(viewHolder.avatar,mContext);
        }catch(Exception e){
            Log.e(LOG_TAG,"Exception while loading thumbnail : " + e.getMessage());
        }

        /*
        URLImageParser p = new URLImageParser(viewHolder.txtMessage, mContext);
        viewHolder.txtMessage.setText(Html.fromHtml(message.GetMessage(),p,null));
        */
        viewHolder.txtMessage.setMovementMethod(LinkMovementMethod.getInstance());
        viewHolder.txtMessage.setText(Html.fromHtml(message.GetMessage()));


        viewHolder.txtDate.setText(String.valueOf(message.GetSendDate()));
        viewHolder.txtUserFrom.setText(message.GetSender().GetUsername());
        viewHolder.txtUserFrom.setTextColor(Color.parseColor("#" + message.GetSender().GetColor()));

        if(message.isPrivate()){
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.colorMessagePrivate));
            viewHolder.txtUserTo.setText(message.GetRecipient().GetUsername());
            viewHolder.txtUserTo.setTextColor(Color.parseColor("#" + message.GetRecipient().GetColor()));
            viewHolder.txtUserTo.setVisibility(View.VISIBLE);

        }else{
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.colorMessagePublic));
            viewHolder.txtUserTo.setVisibility(View.GONE);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
