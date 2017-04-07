package fr.fusoft.frenchyponiescb.events;

import java.util.List;

import fr.fusoft.frenchyponiescb.ponybox.Channel;
import fr.fusoft.frenchyponiescb.ponybox.User;

/**
 * Created by Florent on 27/03/2017.
 */

public class UserListUpdatedEvent {
    public final List<User> users;
    public final String channel;

    public UserListUpdatedEvent(String channel, List<User> users){
        this.users = users;
        this.channel = channel;
    }
}
