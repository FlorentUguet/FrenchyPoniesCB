package fr.fusoft.frenchyponiescb.events;

/**
 * Created by Florent on 27/03/2017.
 */

public class AskFullMessageListEvent {
    public String channel;
    public AskFullMessageListEvent(String channel){
        this.channel = channel;
    }
}
