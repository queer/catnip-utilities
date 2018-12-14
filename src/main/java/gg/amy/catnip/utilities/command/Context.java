package gg.amy.catnip.utilities.command;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.message.Message;
import lombok.Value;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

/**
 * @author amy
 * @since 9/10/18.
 */
@Value
@Accessors(fluent = true)
public class Context {
    private Catnip catnip;
    private Message source;
    
    @SuppressWarnings("UnusedReturnValue")
    public CompletionStage<Message> sendMessage(@Nonnull final String content) {
        return catnip.rest().channel().sendMessage(source.channelId(), content);
    }
}
