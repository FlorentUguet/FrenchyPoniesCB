package fr.fusoft.frenchyponiescb.events;

import fr.fusoft.frenchyponiescb.ponybox.Message;

/**
 * Created by Florent on 26/03/2017.
 */

public class MessageRecievedEvent {
    public final Message message;
    public final String channel;

    public MessageRecievedEvent(Message message) {
        this.message = message;
        this.channel = message.GetChannel();
    }
}
