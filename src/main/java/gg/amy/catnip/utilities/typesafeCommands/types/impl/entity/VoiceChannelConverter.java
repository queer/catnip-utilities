package gg.amy.catnip.utilities.typesafeCommands.types.impl.entity;

import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.VoiceChannel;
import com.mewna.catnip.entity.message.Message;
import gg.amy.catnip.utilities.typesafeCommands.types.MessageConverter;
import gg.amy.catnip.utilities.typesafeCommands.types.TypeConverter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author amy
 * @since 10/1/18.
 */
public class VoiceChannelConverter implements MessageConverter<VoiceChannel> {
    private final TypeConverter<Message, Channel> converter = new ChannelConverter();
    
    @Override
    public Pair<VoiceChannel, String> convert(final Message context, final String input) {
        final Pair<Channel, String> res = converter.convert(context, input);
        if(res.getLeft() != null) {
            if(res.getLeft().isVoice()) {
                return ImmutablePair.of((VoiceChannel) res.getLeft(), res.getRight());
            } else {
                return ImmutablePair.of(null, res.getRight());
            }
        } else {
            return ImmutablePair.of(null, res.getRight());
        }
    }
}
