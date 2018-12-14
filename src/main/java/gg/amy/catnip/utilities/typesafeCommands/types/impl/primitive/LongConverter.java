package gg.amy.catnip.utilities.typesafeCommands.types.impl.primitive;

import com.mewna.catnip.entity.message.Message;
import gg.amy.catnip.utilities.typesafeCommands.types.MessageConverter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author amy
 * @since 10/8/18.
 */
public class LongConverter implements MessageConverter<Long> {
    @Override
    public Pair<Long, String> convert(final Message context, final String input) {
        if(input == null || input.isEmpty()) {
            return ImmutablePair.of(null, input);
        }
        if(input.matches("\\d+(\\s+.*)?")) {
            try {
                final String[] split = input.split("\\s+", 2);
                final String ret = (split.length > 1 ? split[1] : "").trim();
                return ImmutablePair.of(Long.parseLong(split[0]), ret);
            } catch(final NumberFormatException e) {
                context.catnip().logAdapter().warn("Reached catch in IntegerConverter#convert - this shouldn't happen!");
                return ImmutablePair.of(null, input);
            }
        } else {
            // Not a number
            return ImmutablePair.of(null, input);
        }
    }
}
