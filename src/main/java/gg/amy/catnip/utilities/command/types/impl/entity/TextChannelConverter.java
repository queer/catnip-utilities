package gg.amy.catnip.utilities.command.types.impl.entity;

import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.TextChannel;
import com.mewna.catnip.entity.message.Message;
import gg.amy.zoe.ext.command.types.MessageConverter;
import gg.amy.zoe.ext.command.types.TypeConverter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author amy
 * @since 10/1/18.
 */
public class TextChannelConverter implements MessageConverter<TextChannel> {
    private final TypeConverter<Message, Channel> converter = new ChannelConverter();
    
    @Override
    public Pair<TextChannel, String> convert(final Message context, final String input) {
        final Pair<Channel, String> res = converter.convert(context, input);
        if(res.getLeft() != null) {
            if(res.getLeft().isText()) {
                return ImmutablePair.of((TextChannel) res.getLeft(), res.getRight());
            } else {
                return ImmutablePair.of(null, res.getRight());
            }
        } else {
            return ImmutablePair.of(null, res.getRight());
        }
    }
}
