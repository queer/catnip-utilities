package gg.amy.catnip.utilities.menu.component;

import com.google.common.collect.ImmutableSet;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.user.User;
import gg.amy.catnip.utilities.menu.MenuEmoji;
import gg.amy.catnip.utilities.menu.component.impl.ButtonImpl;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author amy
 * @since 10/8/18.
 */
public interface Button {
    static Button left(@Nonnull final MenuEmoji menuEmoji, @Nonnull final BiConsumer<User, Message> consumer) {
        return new ButtonImpl(menuEmoji.leftArrow(), ImmutableSet.of("prev", "left", "<"), consumer);
    }
    
    static Button right(@Nonnull final MenuEmoji menuEmoji, @Nonnull final BiConsumer<User, Message> consumer) {
        return new ButtonImpl(menuEmoji.rightArrow(), ImmutableSet.of("next", "right", ">"), consumer);
    }
    
    static Button cancel(@Nonnull final MenuEmoji menuEmoji, @Nonnull final BiConsumer<User, Message> consumer) {
        return new ButtonImpl(menuEmoji.cancel(), ImmutableSet.of("no", "stop", "cancel"), consumer);
    }
    
    static Button accept(@Nonnull final MenuEmoji menuEmoji, @Nonnull final BiConsumer<User, Message> consumer) {
        return new ButtonImpl(menuEmoji.accept(), ImmutableSet.of("yes", "start", "accept"), consumer);
    }
    
    static Button refresh(@Nonnull final MenuEmoji menuEmoji, @Nonnull final BiConsumer<User, Message> consumer) {
        return new ButtonImpl(menuEmoji.refresh(), ImmutableSet.of("refresh", "reload", "rl"), consumer);
    }
    
    String emoji();
    
    Set<String> aliases();
    
    BiConsumer<User, Message> onClick();
}
