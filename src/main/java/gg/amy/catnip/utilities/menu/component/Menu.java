package gg.amy.catnip.utilities.menu.component;

import com.mewna.catnip.entity.user.User;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author amy
 * @since 10/8/18.
 */
public interface Menu {
    String displayText();
    
    List<Button> buttons();
    
    void accept(@Nonnull final User owner, @Nonnull final String channelId);
}
