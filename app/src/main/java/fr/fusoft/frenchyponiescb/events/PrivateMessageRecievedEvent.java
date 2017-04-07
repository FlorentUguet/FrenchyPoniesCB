package fr.fusoft.frenchyponiescb.events;

import fr.fusoft.frenchyponiescb.ponybox.Message;

/**
 * Created by Florent on 27/03/2017.
 */

public class PrivateMessageRecievedEvent {
    public final String to;
    public final String channel;
    public final Message message;

    public PrivateMessageRecievedEvent(String channel, String to, Message message){
        this.to = to;
        this.message = message;
        this.channel = channel;
    }
}
