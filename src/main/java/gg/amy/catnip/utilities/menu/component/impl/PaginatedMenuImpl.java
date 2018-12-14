package gg.amy.catnip.utilities.menu.component.impl;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.Catnip;
import gg.amy.catnip.utilities.menu.MenuEmoji;
import gg.amy.catnip.utilities.menu.MenuExtension;
import gg.amy.catnip.utilities.menu.component.Button;
import gg.amy.catnip.utilities.menu.component.PaginatedMenu;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author amy
 * @since 10/9/18.
 */
@Accessors(fluent = true)
public class PaginatedMenuImpl extends MenuImpl implements PaginatedMenu {
    @Getter
    private final List<String> pageData = new ArrayList<>();
    private final String displayText;
    @Getter
    private int page;
    
    public PaginatedMenuImpl(@Nonnull final Catnip catnip, @Nonnull final String displayText,
                             @Nonnull final Collection<String> data) {
        super(catnip, "LOADING...", ImmutableList.of());
        this.displayText = displayText;
        pageData.addAll(data);
        //noinspection ConstantConditions
        final MenuEmoji menuEmoji = catnip.extensionManager().extension(MenuExtension.class).menuEmoji();
        createArrows(menuEmoji);
        // Don't need to do anything other than create it ^^
        // MenuImpl handles it for us
        createCancel(menuEmoji);
        initialRender();
    }
    
    @SuppressWarnings("unused")
    public PaginatedMenuImpl(@Nonnull final Catnip catnip, @Nonnull final String displayText,
                             @Nonnull final Collection<String> data,
                             @Nonnull final Supplier<CompletableFuture<List<String>>> refresher) {
        super(catnip, "LOADING...", ImmutableList.of());
        this.displayText = displayText;
        pageData.addAll(data);
        //noinspection ConstantConditions
        final MenuEmoji menuEmoji = catnip.extensionManager().extension(MenuExtension.class).menuEmoji();
        createArrows(menuEmoji);
        buttons().add(Button.refresh(menuEmoji, (__, ___) ->
                refresher.get().thenAccept(newData -> {
                    pageData.clear();
                    pageData.addAll(newData);
                    render();
                })));
        createCancel(menuEmoji);
        initialRender();
    }
    
    private void createArrows(final MenuEmoji menuEmoji) {
        buttons().add(Button.left(menuEmoji, (__, ___) -> previousPage()));
        buttons().add(Button.right(menuEmoji, (__, ___) -> nextPage()));
    }
    
    private void createCancel(final MenuEmoji menuEmoji) {
        // Don't need to do anything other than create it ^^
        // MenuImpl handles it for us
        buttons().add(Button.cancel(menuEmoji, (__, ___) -> {
        }));
    }
    
    private void initialRender() {
        // We wait before rendering to make sure that the com.mewna.catnip.utilities.menu has actually loaded
        // ie. that the message has been created
        catnip().vertx().setTimer(100L, __ -> {
            if(channelId().get() != null && messageId().get() != null) {
                render();
            } else {
                initialRender();
            }
        });
    }
    
    @Override
    public int pages() {
        return pageData.size();
    }
    
    @Override
    public void nextPage() {
        ++page;
        if(page > pageData.size() - 1) {
            page = 0;
        }
        render();
    }
    
    @Override
    public void previousPage() {
        --page;
        if(page < 0) {
            page = pageData.size() - 1;
        }
        render();
    }
    
    private void render() {
        catnip().rest().channel().editMessage(channelId().get(), messageId().get(), displayText + "\n\n" + pageData.get(page)
                + "\n\n" + String.format("Page %s / %s", page + 1, pages()) + "\n\nThis menu will time out in **"
                + TimeUnit.MILLISECONDS.toMinutes(TIMEOUT_MS) + " minutes**.");
    }
}
