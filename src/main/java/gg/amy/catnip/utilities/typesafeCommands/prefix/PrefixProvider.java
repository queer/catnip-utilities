package gg.amy.catnip.utilities.typesafeCommands.prefix;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author amy
 * @since 9/10/18.
 */
@FunctionalInterface
public interface PrefixProvider {
    @Nonnull
    List<String> providePrefix(@Nullable String guildId, @Nonnull String channelId, @Nonnull String userId);
}
