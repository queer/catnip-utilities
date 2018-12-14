package gg.amy.catnip.utilities.command.types;

import com.mewna.catnip.entity.message.Message;

/**
 * @author amy
 * @since 9/11/18.
 */
@SuppressWarnings("WeakerAccess")
@FunctionalInterface
public interface MessageConverter<T> extends TypeConverter<Message, T> {
}
