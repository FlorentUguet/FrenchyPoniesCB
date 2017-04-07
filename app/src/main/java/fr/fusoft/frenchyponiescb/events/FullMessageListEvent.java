package fr.fusoft.frenchyponiescb.events;

import java.util.List;

import fr.fusoft.frenchyponiescb.ponybox.Message;

/**
 * Created by Florent on 27/03/2017.
 */

public class FullMessageListEvent {
    public final List<Message> messages;
    public final String channel;

    public FullMessageListEvent(String channel, List<Message> messages) {
        this.messages = messages;
        this.channel = channel;
    }
}
