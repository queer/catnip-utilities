package gg.amy.catnip.utilities.waiter;

import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.shard.EventType;

/**
 * @author AdrianTodt
 * @since 1/15/18.
 */
public class EventExtension extends AbstractExtension {
    public EventExtension() {
        super("eventWaiter");
    }

    public <T> WaitingEventBuilder<T> waitForEvent(EventType<T> type) {
        return new WaitingEventBuilder<T>(catnip(), type);
    }
}
