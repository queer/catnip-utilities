package gg.amy.catnip.utilities.typesafeCommands.types.impl.entity;

import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.user.User;
import gg.amy.catnip.utilities.typesafeCommands.types.MessageConverter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author amy
 * @since 1/18/19.
 */
public class GuildConverter implements MessageConverter<Guild> {
    @Override
    public Pair<Guild, String> convert(final Message context, final String input) {
        if(input == null) {
            //noinspection ConstantConditions
            return ImmutablePair.of(null, input);
        }
        if(input.matches("^\\d{17,21}(.*)")) {
            final String[] split = input.split("\\s+", 2);
            final String snowflake = split[0].replaceAll("\\D+", "");
            final String ret = (split.length > 1 ? split[1] : "").trim();
            // Check message
            if(context.guildId() != null && context.guildId().equalsIgnoreCase(snowflake)) {
                return ImmutablePair.of(context.guild(), ret);
            }
            // Search in cache
            final Guild guild = context.catnip().cache().guild(snowflake);
            if(guild != null) {
                return ImmutablePair.of(guild, ret);
            }
        }
        return ImmutablePair.of(null, input);
    }
}
