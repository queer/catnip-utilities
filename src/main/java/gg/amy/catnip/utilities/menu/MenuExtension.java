package gg.amy.catnip.utilities.menu;

import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.extension.AbstractExtension;
import gg.amy.catnip.utilities.menu.component.Button;
import gg.amy.catnip.utilities.menu.component.Menu;
import gg.amy.catnip.utilities.menu.component.PaginatedMenu;
import gg.amy.catnip.utilities.menu.component.impl.MenuImpl.MenuBuilder;
import gg.amy.catnip.utilities.menu.component.impl.PaginatedMenuImpl;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author amy
 * @since 10/8/18.
 */
@Accessors(fluent = true)
public class MenuExtension extends AbstractExtension {
    @Getter
    private final MenuEmoji menuEmoji;
    
    public MenuExtension() {
        this(new MenuEmoji());
    }
    
    @SuppressWarnings("WeakerAccess")
    public MenuExtension(@Nonnull final MenuEmoji menuEmoji) {
        super("menus");
        this.menuEmoji = menuEmoji;
    }
    
    @Nonnull
    @CheckReturnValue
    @SuppressWarnings("WeakerAccess")
    public MenuBuilder createMenuBuilder() {
        return new MenuBuilder(catnip());
    }
    
    @Nonnull
    @CheckReturnValue
    public Menu createYesNoMenu(@Nonnull final String displayText, @Nonnull final BiConsumer<User, Message> onAccept,
                                @Nonnull final BiConsumer<User, Message> onCancel) {
        return createMenuBuilder()
                .displayText(displayText)
                .button(Button.accept(menuEmoji, onAccept))
                .button(Button.cancel(menuEmoji, onCancel))
                .build();
    }
    
    @Nonnull
    @CheckReturnValue
    public PaginatedMenu createPaginatedMenu(@Nonnull final String displayText, @Nonnull final List<String> pages) {
        return new PaginatedMenuImpl(catnip(), displayText, pages);
    }
}
