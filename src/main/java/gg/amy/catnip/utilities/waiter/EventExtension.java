package gg.amy.catnip.utilities.waiter;

import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.shard.EventType;
import lombok.experimental.Accessors;

/**
 * @author AdrianTodt
 * @since 1/15/18.
 */
@Accessors(fluent = true)
public class EventExtension extends AbstractExtension {
    public EventExtension() {
        super("eventWaiter");
    }

    public <T> WaitingEventBuilder<T> waitForEvent(EventType<T> type) {
        return new WaitingEventBuilder<T>(catnip(), type);
    }
}