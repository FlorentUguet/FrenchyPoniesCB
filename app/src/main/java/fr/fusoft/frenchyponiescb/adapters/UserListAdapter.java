package fr.fusoft.frenchyponiescb.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fr.fusoft.frenchyponiescb.R;
import fr.fusoft.frenchyponiescb.ponybox.User;

/**
 * Created by Florent on 27/03/2017.
 */

public class UserListAdapter extends ArrayAdapter<User> implements View.OnClickListener{

    private List<User> users;
    Context mContext;

    String LOG_TAG = "UserListAdapter";

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        ImageView avatar;
    }

    public UserListAdapter(List<User> data, Context context) {
        super(context, R.layout.item_user, data);
        this.users = data;
        this.mContext=context;
    }

    public void setUsers(List<User> users)
    {
        this.users.clear();
        this.users.addAll(users);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        User user = (User)object;
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User user = users.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        UserListAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new UserListAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_user, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.textViewUser);
            viewHolder.avatar = (ImageView) convertView.findViewById(R.id.imageViewAvatar);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (UserListAdapter.ViewHolder) convertView.getTag();
            result=convertView;
        }

        try {
            user.loadAvatar(viewHolder.avatar,mContext);
        }catch(Exception e){
            Log.e(LOG_TAG,"Exception while loading thumbnail : " + e.getMessage());
        }

        lastPosition = position;

        viewHolder.txtName.setText(user.getUsername());
        viewHolder.txtName.setTextColor(Color.parseColor("#" + user.getColor()));

        // Return the completed view to render on screen
        return convertView;
    }

}
