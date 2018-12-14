package gg.amy.catnip.utilities.typesafeCommands.types.impl.entity;

import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.message.Message;
import gg.amy.catnip.utilities.typesafeCommands.types.MessageConverter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;

/**
 * @author amy
 * @since 10/1/18.
 */
public class ChannelConverter implements MessageConverter<Channel> {
    @Override
    public Pair<Channel, String> convert(final Message context, final String input) {
        if(input == null) {
            //noinspection ConstantConditions
            return ImmutablePair.of(null, input);
        }
        if(input.matches("^((<#!?)?\\d{17,21}(>)?)(.*)")) {
            final String[] split = input.split("\\s+", 2);
            final String snowflake = split[0].replaceAll("\\D+", "");
            final String ret = (split.length > 1 ? split[1] : "").trim();
            // Search in cache
            final Channel channel = context.catnip().cache().channel(Objects.requireNonNull(context.guildId()), snowflake);
            if(channel != null) {
                return ImmutablePair.of(channel, ret);
            }
        }
        return ImmutablePair.of(null, input);
    }
}
