package fr.fusoft.frenchyponiescb.events;

/**
 * Created by Florent on 31/03/2017.
 */

public class AskOlderMessagesEvent {
    String channel;

    public AskOlderMessagesEvent(String channel)
    {
        this.channel = channel;
    }
}
