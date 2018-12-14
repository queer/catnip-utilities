package gg.amy.catnip.utilities.menu.component.impl;

import com.google.common.collect.ImmutableList;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.message.BulkDeletedMessages;
import com.mewna.catnip.entity.message.DeletedMessage;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.ReactionUpdate;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.shard.DiscordEvent;
import gg.amy.catnip.utilities.FutureUtil;
import gg.amy.catnip.utilities.menu.MenuExtension;
import gg.amy.catnip.utilities.menu.component.Button;
import gg.amy.catnip.utilities.menu.component.Menu;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author amy
 * @since 10/9/18.
 */
@Getter
@Accessors(fluent = true)
public class MenuImpl implements Menu {
    protected static final long TIMEOUT_MS = TimeUnit.MINUTES.toMillis(5L);
    private final List<MessageConsumer<?>> consumers = new CopyOnWriteArrayList<>();
    @Getter(AccessLevel.PACKAGE)
    private final Catnip catnip;
    @Getter(AccessLevel.PROTECTED)
    private final AtomicReference<String> messageId = new AtomicReference<>();
    @Getter(AccessLevel.PROTECTED)
    private final AtomicReference<String> channelId = new AtomicReference<>();
    @Getter(AccessLevel.PROTECTED)
    private final AtomicBoolean disabled = new AtomicBoolean(false);
    @Setter(AccessLevel.PROTECTED)
    private String displayText;
    @Setter(AccessLevel.PROTECTED)
    private List<Button> buttons;
    
    @SuppressWarnings("WeakerAccess")
    public MenuImpl(@Nonnull final Catnip catnip, @Nonnull final String displayText, @Nonnull final List<Button> buttons) {
        this.catnip = catnip;
        this.displayText = displayText;
        // We let this be mutable so that subclasses can mess with it
        this.buttons = new ArrayList<>(buttons);
    }
    
    @Override
    public void accept(@Nonnull final User owner, @Nonnull final String channelId) {
        createMenuDisplay(catnip, channelId).thenAccept(message -> {
            messageId.set(message.id());
            this.channelId.set(channelId);
            createButtons(catnip, channelId, message.id()).thenAccept(__ -> {
                consumers.add(catnip.on(DiscordEvent.MESSAGE_REACTION_ADD, r -> handleReaction(catnip, message, owner, r)));
                consumers.add(catnip.on(DiscordEvent.MESSAGE_CREATE, msg -> handleMessageCreate(catnip, message, owner, msg)));
                consumers.add(catnip.on(DiscordEvent.MESSAGE_DELETE, msg -> handleMessageDelete(message, msg)));
                consumers.add(catnip.on(DiscordEvent.MESSAGE_DELETE_BULK, msgs -> handleMessageBulkDelete(message, msgs)));
                catnip.vertx().setTimer(TIMEOUT_MS, ___ -> handleCancel("Menu timed out."));
            });
        });
    }
    
    private CompletionStage<Message> createMenuDisplay(final Catnip catnip, final String channelId) {
        return catnip.rest().channel().sendMessage(channelId, displayText + "\n\nThis menu will time out in **5 minutes**.");
    }
    
    private CompletionStage<Void> createButtons(final Catnip catnip, final String channelId, final String messageId) {
        final Collection<CompletionStage<Void>> futures = new ArrayList<>();
        buttons().forEach(b -> {
            final CompletionStage<Void> future = catnip.rest().channel().addReaction(channelId, messageId, b.emoji());
            futures.add(future);
        });
        return FutureUtil.awaitAll(futures);
    }
    
    @SuppressWarnings("TypeMayBeWeakened")
    private void handleReaction(final Catnip catnip, final Message menuMessage, final User owner, final ReactionUpdate reaction) {
        if(reaction.messageId().equals(menuMessage.id()) && reaction.userId().equals(owner.id())) {
            buttons().stream().filter(e -> reaction.emoji().is(e.emoji()))
                    .forEach(e -> e.onClick().accept(catnip.cache().user(reaction.userId()), menuMessage));
            catnip.rest().channel()
                    .deleteUserReaction(menuMessage.channelId(), menuMessage.id(), owner.id(), reaction.emoji())
                    .thenAccept(__ -> {
                        //noinspection ConstantConditions
                        if(reaction.emoji().is(catnip.extensionManager().extension(MenuExtension.class).menuEmoji().cancel())) {
                            handleCancel("Aborted.");
                        }
                    });
        }
    }
    
    @SuppressWarnings("TypeMayBeWeakened")
    private void handleMessageCreate(final Catnip catnip, final Message menuMessage, final User owner, final Message msg) {
        if(msg.author().id().equals(owner.id())) {
            final int[] count = {0};
            buttons().stream().filter(e -> e.aliases().contains(msg.content().trim().toLowerCase()))
                    .forEach(e -> {
                        e.onClick().accept(catnip.cache().user(owner.id()), menuMessage);
                        ++count[0];
                    });
            if(count[0] > 0) {
                catnip.rest().channel().deleteMessage(msg.channelId(), msg.id());
            }
            
            //noinspection ConstantConditions
            buttons.stream().filter(e -> e.emoji().equals(catnip.extensionManager().extension(MenuExtension.class)
                    .menuEmoji().cancel()))
                    .filter(e -> e.aliases().contains(msg.content().trim().toLowerCase()))
                    .findFirst().ifPresent(e -> handleCancel("Aborted."));
        }
    }
    
    private void handleCancel(@Nullable final String message) {
        if(!disabled.get()) {
            disabled.set(true);
            consumers.forEach(MessageConsumer::unregister);
            if(channelId.get() != null && messageId.get() != null) {
                catnip.rest().channel().deleteMessage(channelId.get(), messageId.get());
            }
            if(message != null && !consumers.isEmpty()) {
                if(channelId.get() != null) {
                    catnip.rest().channel().sendMessage(channelId.get(), message);
                }
            }
            consumers.clear();
        }
    }
    
    @SuppressWarnings("TypeMayBeWeakened")
    private void handleMessageDelete(final Message menuMessage, final DeletedMessage msg) {
        if(msg.id().equals(menuMessage.id())) {
            // Don't try to do stuff with the com.mewna.catnip.utilities.menu message if it was just deleted
            messageId.set(null);
            handleCancel(null);
        }
    }
    
    @SuppressWarnings("TypeMayBeWeakened")
    private void handleMessageBulkDelete(final Message menuMessage, final BulkDeletedMessages msgs) {
        if(msgs.ids().stream().anyMatch(e -> e.equals(menuMessage.id()))) {
            handleCancel(null);
        }
    }
    
    public static class MenuBuilder {
        private final Catnip catnip;
        private final Collection<Button> buttons = new ArrayList<>();
        private String displayText;
        
        public MenuBuilder(final Catnip catnip) {
            this.catnip = catnip;
        }
        
        @CheckReturnValue
        public MenuBuilder displayText(@Nonnull final String text) {
            displayText = text;
            return this;
        }
        
        @CheckReturnValue
        public MenuBuilder button(@Nonnull final Button button) {
            buttons.add(button);
            return this;
        }
        
        @CheckReturnValue
        public Menu build() {
            return new MenuImpl(catnip, displayText, ImmutableList.copyOf(buttons));
        }
    }
}
