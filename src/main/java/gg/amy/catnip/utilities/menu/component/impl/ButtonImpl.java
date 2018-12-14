package gg.amy.catnip.utilities.menu.component.impl;

import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.user.User;
import gg.amy.catnip.utilities.menu.component.Button;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author amy
 * @since 10/9/18.
 */
@Value
@Accessors(fluent = true)
public class ButtonImpl implements Button {
    private final String emoji;
    private final Set<String> aliases;
    private final BiConsumer<User, Message> onClick;
}
