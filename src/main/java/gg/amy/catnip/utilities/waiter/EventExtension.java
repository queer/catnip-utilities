package gg.amy.catnip.utilities.waiter;

import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.shard.EventType;
import lombok.experimental.Accessors;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author AdrianTodt
 * @since 1/15/18.
 */
@Accessors(fluent = true)
public class EventExtension extends AbstractExtension {
    private final ScheduledExecutorService threadpool;

    public EventExtension() {
        this(Executors.newSingleThreadScheduledExecutor());
    }

    public EventExtension(ScheduledExecutorService threadpool) {
        super("eventWaiter");
        this.threadpool = threadpool;
    }

    public <T> WaitingEventBuilder<T> waitForEvent(EventType<T> type) {
        return new WaitingEventBuilder<T>(catnip(), threadpool, type);
    }

    public void shutdown() {
        threadpool.shutdown();
    }
}
