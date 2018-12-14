package gg.amy.catnip.utilities.typesafeCommands;

import com.mewna.catnip.entity.channel.Channel;
import com.mewna.catnip.entity.channel.TextChannel;
import com.mewna.catnip.entity.channel.VoiceChannel;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.shard.DiscordEvent;
import gg.amy.zoe.ext.command.parse.ArgParser;
import gg.amy.zoe.ext.command.parse.impl.GenericArgParser;
import gg.amy.zoe.ext.command.prefix.PrefixProvider;
import gg.amy.zoe.ext.command.types.TypeConverter;
import gg.amy.zoe.ext.command.types.impl.entity.ChannelConverter;
import gg.amy.zoe.ext.command.types.impl.entity.TextChannelConverter;
import gg.amy.zoe.ext.command.types.impl.entity.UserConverter;
import gg.amy.zoe.ext.command.types.impl.entity.VoiceChannelConverter;
import gg.amy.zoe.ext.command.types.impl.primitive.BooleanConverter;
import gg.amy.zoe.ext.command.types.impl.primitive.IntegerConverter;
import gg.amy.zoe.ext.command.types.impl.primitive.LongConverter;
import gg.amy.zoe.ext.command.types.impl.primitive.StringConverter;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 9/9/18.
 */
@Accessors(fluent = true)
@SuppressWarnings("unused")
public class TypesafeCommandExtension extends AbstractExtension {
    private final Map<String, CommandContainer> commands = new ConcurrentHashMap<>();
    @Getter
    private final Map<Class<?>, TypeConverter<Message, ?>> converters = new ConcurrentHashMap<>();
    @Getter
    private final ArgParser parser;
    @Getter
    @Setter
    private PrefixProvider prefixProvider;
    @SuppressWarnings("FieldCanBeLocal")
    private MessageConsumer<Message> consumer;
    
    public TypesafeCommandExtension(@Nonnull final String defaultPrefix) {
        this((_guild, _channel, _user) -> Collections.singletonList(defaultPrefix));
    }
    
    public TypesafeCommandExtension(@Nonnull final String defaultPrefix, @Nonnull final ArgParser argParser) {
        this((_guild, _channel, _user) -> Collections.singletonList(defaultPrefix), argParser);
    }
    
    @SuppressWarnings("WeakerAccess")
    public TypesafeCommandExtension(@Nonnull final PrefixProvider provider) {
        this(provider, new GenericArgParser());
    }
    
    @SuppressWarnings("WeakerAccess")
    public TypesafeCommandExtension(@Nonnull final PrefixProvider provider, @Nonnull final ArgParser argParser) {
        super("commands");
        prefixProvider = provider;
        parser = argParser;
    }
    
    private static InputStream fromClass(final Class<?> c) {
        return TypesafeCommandExtension.class.getResourceAsStream('/' + c.getName().replace(".", "/") + ".class");
    }
    
    @Override
    public void start() {
        catnip().logAdapter().info("Loading commands extension...");
        
        registerConverter(User.class, new UserConverter())
                .registerConverter(Channel.class, new ChannelConverter())
                .registerConverter(TextChannel.class, new TextChannelConverter())
                .registerConverter(VoiceChannel.class, new VoiceChannelConverter())
                .registerConverter(String.class, new StringConverter())
                .registerConverter(Integer.class, new IntegerConverter())
                .registerConverter(Boolean.class, new BooleanConverter())
                .registerConverter(Long.class, new LongConverter())
        ;
        
        consumer = catnip().on(DiscordEvent.MESSAGE_CREATE, this::invoke);
        // Search for and load commands
        try(final ScanResult res = new ClassGraph().enableAllInfo().scan()) {
            res.getClassesWithMethodAnnotation(Command.class.getName())
                    .stream().map(ClassInfo::loadClass).forEach(this::magic);
        }
        catnip().logAdapter().info("Loaded {} commands!", commands.size());
    }
    
    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public <T> TypesafeCommandExtension registerConverter(final Class<T> cls, final TypeConverter<Message, T> converter) {
        if(converters.containsKey(cls)) {
            throw new IllegalArgumentException("Already registered a converter for " + cls.getName() + "!!");
        }
        converters.put(cls, converter);
        return this;
    }
    
    private void invoke(final Message message) {
        // TODO: Remove
        if(!message.author().id().equals("128316294742147072")) {
            return;
        }
        final List<String> prefixes = prefixProvider.providePrefix(message.guildId(), message.channelId(), message.author().id());
        String content = message.content();
        String prefix = null;
        for(final String p : prefixes) {
            if(content.startsWith(p)) {
                prefix = p;
                break;
            }
        }
        if(prefix == null) {
            return;
        }
        content = content.substring(prefix.length()).trim();
        // The black magic begins! :D
        final String[] cmdWithArgstr = content.split("\\s+", 2);
        final String cmd = cmdWithArgstr[0];
        final String argstr = cmdWithArgstr.length > 1 ? cmdWithArgstr[1] : null;
        final Optional<CommandContainer> containerOptional = Optional.ofNullable(commands.get(cmd));
        
        if(containerOptional.isPresent()) {
            final CommandContainer container = containerOptional.get();
            final Map<String, Object> args = new LinkedHashMap<>();
            final Context context = new Context(catnip(), message);
            args.put("__context", context);
            
            final Map<String, Object> parsed = parser.parse(this, container, message, argstr);
            // We can't assume that the map will be ordered, so we enforce our own ordering
            container.typeInfo().entrySet().stream().sequential()
                    .forEach(e -> {
                        // Booleans are special
                        // TODO: This shouldn't be happening here
                        if(e.getValue().typeClass().equals(Boolean.class)) {
                            args.put(e.getKey(), parsed.containsKey(e.getKey()));
                        } else {
                            args.put(e.getKey(), parsed.get(e.getKey()));
                        }
                    });
            
            try {
                container.method.invoke(container.container, args.values().toArray(new Object[0]));
            } catch(final IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            } catch(final IllegalArgumentException e) {
                catnip().logAdapter().error("Failed calling command {} with args {} - did you give it a bad type conversion???",
                        container.name, args, e);
                catnip().logAdapter().error("Expected {} args, gave {}!", container.typeInfo.size(), args.size());
            }
        }
    }
    
    private void magic(final Class<?> cls) {
        final Method[] declMethods = cls.getDeclaredMethods();
        
        Arrays.stream(declMethods).filter(e -> e.isAnnotationPresent(Command.class)).forEach(m -> {
            if(!m.getParameterTypes()[0].equals(Context.class)) {
                catnip().logAdapter()
                        .error("Method {}#{} is annotated as @Command but has no Context arg, skipping!",
                                cls.getName(), m.getName());
            } else {
                final Map<String, ParamTypeInfo> params = extractParams(cls, m);
                final Command annotation = m.getAnnotation(Command.class);
                for(final String name : annotation.names()) {
                    if(commands.containsKey(name)) {
                        catnip().logAdapter().warn("Already registered command {}, ignoring...", name);
                    } else {
                        final Object container;
                        try {
                            container = cls.getConstructor().newInstance();
                        } catch(final InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        }
                        commands.put(name, new CommandContainer(name, container, m, params));
                        catnip().logAdapter().info("Registered command {} from {} with params {}", name, cls.getName(),
                                params.entrySet().stream().map(e -> e.getKey() + " => " + e.getValue().typeName).collect(Collectors.toList()));
                    }
                }
            }
        });
    }
    
    private Map<String, ParamTypeInfo> extractParams(final Class<?> cls, final Method method) {
        // Preserve insertion order
        final Map<String, ParamTypeInfo> res = new LinkedHashMap<>();
        Arrays.stream(method.getParameters()).filter(e -> !e.getType().equals(Context.class)).forEach(p -> {
            final var parameterizedType = p.getParameterizedType();
            final var typeName = parameterizedType.getTypeName();
            res.put(p.getName(), new ParamTypeInfo(typeName, p.getType()));
        });
        return res;
    }
    
    @Override
    public void stop() {
        consumer.unregister();
    }
    
    @Value
    @Accessors(fluent = true)
    public static class CommandContainer {
        private String name;
        private Object container;
        private Method method;
        private Map<String, ParamTypeInfo> typeInfo;
    }
    
    @Value
    @ToString
    @Accessors(fluent = true)
    public static class ParamTypeInfo {
        private String typeName;
        private Class<?> typeClass;
    }
}
