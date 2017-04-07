package fr.fusoft.frenchyponiescb.events;

import fr.fusoft.frenchyponiescb.ponybox.Message;

/**
 * Created by Florent on 26/03/2017.
 */

public class SendMessageEvent {

    public final String message;
    public final String to;
    public final String channel;

    public SendMessageEvent(String message, String to, String channel) {
        this.message = message;
        this.to = to;
        this.channel = channel;
    }
}
