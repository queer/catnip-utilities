package gg.amy.catnip.utilities.typesafeCommands.types.impl.entity;

import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.user.User;
import gg.amy.catnip.utilities.typesafeCommands.types.MessageConverter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author amy
 * @since 9/11/18.
 */
public class UserConverter implements MessageConverter<User> {
    @Override
    public Pair<User, String> convert(final Message context, final String input) {
        if(input == null) {
            //noinspection ConstantConditions
            return ImmutablePair.of(null, input);
        }
        if(input.matches("^((<@!?)?\\d{17,21}(>)?)(.*)")) {
            final String[] split = input.split("\\s+", 2);
            final String snowflake = split[0].replaceAll("\\D+", "");
            final String ret = (split.length > 1 ? split[1] : "").trim();
            // Check author
            if(context.author().id().equals(snowflake)) {
                return ImmutablePair.of(context.author(), ret);
            }
            // Check mentions
            for(final User user : context.mentionedUsers()) {
                if(user.id().equals(snowflake)) {
                    return ImmutablePair.of(user, ret);
                }
            }
            // Search in cache
            final User user = context.catnip().cache().user(snowflake);
            if(user != null) {
                return ImmutablePair.of(user, ret);
            }
        }
        return ImmutablePair.of(null, input);
    }
}
