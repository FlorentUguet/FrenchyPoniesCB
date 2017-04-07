package fr.fusoft.frenchyponiescb.events;

import fr.fusoft.frenchyponiescb.ponybox.Channel;

/**
 * Created by Florent on 26/03/2017.
 */

public class ChannelJoinedEvent {
    public final Channel channel;

    public ChannelJoinedEvent(Channel channel) {
        this.channel = channel;
    }
}
