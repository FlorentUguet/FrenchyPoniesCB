package fr.fusoft.frenchyponiescb.events;

import java.util.List;

import fr.fusoft.frenchyponiescb.ponybox.Message;

/**
 * Created by Florent on 31/03/2017.
 */

public class OlderMessagesRecievedEvent {
    public String channel;
    public List<Message> messages;

    public OlderMessagesRecievedEvent(String channel, List<Message> messages)
    {
        this.messages = messages;
        this.channel = channel;
    }
}
