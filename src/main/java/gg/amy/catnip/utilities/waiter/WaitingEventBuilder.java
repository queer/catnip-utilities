package gg.amy.catnip.utilities.waiter;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.event.EventType;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author AdrianTodt
 * @since 1/15/18.
 */
@Accessors(fluent = true)
@RequiredArgsConstructor
public class WaitingEventBuilder<T> {
    private final Catnip catnip;
    private final EventType<T> type;

    @Getter
    private long timeout;
    @Getter
    private TimeUnit unit;
    @Getter
    private Runnable timeoutAction;
    @Getter
    @Setter
    private Predicate<T> condition;

    public WaitingEventBuilder<T> timeout(final long timeout, @Nonnull final TimeUnit unit) {
        return timeout(timeout, unit, null);
    }

    public WaitingEventBuilder<T> timeout(final long timeout, @Nonnull final TimeUnit unit,
                                          @Nullable final Runnable timeoutAction) {
        this.timeout = timeout;
        this.unit = unit;
        this.timeoutAction = timeoutAction;

        return this;
    }

    public MessageConsumer<T> action(final Consumer<T> action) {
        final MessageConsumer<T> consumer = catnip.on(type);

        final Long timerId;
        if(timeout <= 0 || unit == null) {
            timerId = null;
        } else {
            timerId = catnip.vertx().setTimer(unit.toMillis(timeout), __ -> {
                consumer.unregister();

                if(timeoutAction != null) {
                    timeoutAction.run();
                }
            });
        }

        consumer.handler(message -> {
            final T body = message.body();

            if(condition != null && !condition.test(body)) {
                return;
            }

            consumer.unregister();

            if(timerId != null) {
                catnip.vertx().cancelTimer(timerId);
            }

            action.accept(body);
        });

        return consumer;
    }
}
