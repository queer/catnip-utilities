package gg.amy.catnip.utilities.waiter;

import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.shard.event.EventType;

/**
 * @author AdrianTodt
 * @since 1/15/18.
 */
public class EventExtension extends AbstractExtension {
    public EventExtension() {
        super("eventWaiter");
    }

    public <T> WaitingEventBuilder<T> waitForEvent(final EventType<T> type) {
        return new WaitingEventBuilder<>(catnip(), type);
    }
}
