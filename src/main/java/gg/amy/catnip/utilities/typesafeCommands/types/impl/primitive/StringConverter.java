package gg.amy.catnip.utilities.typesafeCommands.types.impl.primitive;

import com.mewna.catnip.entity.message.Message;
import gg.amy.zoe.ext.command.types.MessageConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author amy
 * @since 10/1/18.
 */
public class StringConverter implements MessageConverter<String> {
    @Override
    public Pair<String, String> convert(final Message context, final String input) {
        if(input == null || input.isEmpty()) {
            return ImmutablePair.of(null, input);
        }
        if(input.startsWith("\"")) {
            // Quoted string, attempt to match
            final int quotes = StringUtils.countMatches(input, '"');
            if(quotes > 1) {
                // Parse out first quoted string
                final String str = StringUtils.substringBetween(input, "\"", "\"");
                return ImmutablePair.of(str, input.substring(str.length() + 2));
            } else {
                // Only a single quote, just take first value
                final String[] split = input.split("\\s+", 2);
                final String ret = (split.length > 1 ? split[1] : "").trim();
                return ImmutablePair.of(split[0], ret);
            }
        } else {
            // Only one quote, just take the first value
            final String[] split = input.split("\\s+", 2);
            final String ret = (split.length > 1 ? split[1] : "").trim();
            return ImmutablePair.of(split[0], ret);
        }
    }
}
