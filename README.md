# catnip-utilities

Some stuff to make using [catnip](https://github.com/mewna/catnip) easier.

Current features:
- Typesafe commands
- Menus + pagination

## Usage:

### Typesafe commands

Remember to pass `-parameters` to `javac` when compiling, otherwise it won't work!

```Java
catnip.loadExtension(new TypesafeCommandsExtension("!", new UnixArgParser()));
// or
catnip.loadExtension(new TypesafeCommandsExtension("!", new GenericArgParser()));
```
```Java
public class Commands {
    @Command(names = "ping")
    public void ping(final Context context) {
        context.source().channel().sendMessage("pong! <3");
    }
}
```

### Menus + pagination

Example: A command that paginates members based on when they joined

```Java
@Command(names = "members")
public void members(final Context context, final String guild) {
    final String guildId = guild == null ? context.source().guildId() : guild;
    final Guild g = context.catnip().cache().guild(guildId);
    final List<Member> members = new ArrayList<>(g.members().snapshot());
    members.sort(Comparator.comparing(Member::joinedAt));
    final List<String> pages = Lists.partition(members, 25).stream()
            .map(e -> {
                StringBuilder sb = new StringBuilder("```\n");
                e.stream().map(m -> context.catnip().cache().user(m.id())).filter(Objects::nonNull)
                        .forEach(u -> sb.append("- ").append(u.username()).append('#').append(u.discriminator())
                                .append(" (").append(u.id()).append(")\n"));
                sb.append("```");
                return sb.toString();
            })
            .collect(Collectors.toList());
    context.catnip().extensionManager().extension(MenuExtension.class)
            .createPaginatedMenu("Members of **" + g.name() + "**:", ImmutableList.copyOf(pages))
            .accept(context.source().author(), context.source().channelId());
}
```