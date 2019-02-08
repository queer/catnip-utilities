package gg.amy.catnip.utilities;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.cache.view.NamedCacheView;
import com.mewna.catnip.entity.channel.Category;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.channel.TextChannel;
import com.mewna.catnip.entity.channel.VoiceChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.GuildBan;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.misc.Emoji;
import com.mewna.catnip.entity.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author AdrianTodt
 * @since 02/08/19.
 */
@SuppressWarnings({"Duplicates", "unused", "WeakerAccess"})
public final class FinderUtil {
    public final static Pattern DISCORD_ID = Pattern.compile("\\d{1,20}"); // ID
    public final static Pattern FULL_USER_REF = Pattern.compile("(\\S.{0,30}\\S)\\s*#(\\d{4})"); // $1 -> username, $2 -> discriminator
    public final static Pattern USER_MENTION = Pattern.compile("<@!?(\\d{1,20})>"); // $1 -> ID
    public final static Pattern CHANNEL_MENTION = Pattern.compile("<#(\\d{1,20})>"); // $1 -> ID
    public final static Pattern ROLE_MENTION = Pattern.compile("<@&(\\d{1,20})>"); // $1 -> ID
    public final static Pattern EMOTE_MENTION = Pattern.compile("<:(.{2,32}):(\\d{1,20})>");

    public static List<User> findUsers(String query, Catnip catnip) {
        Matcher userMention = USER_MENTION.matcher(query);
        Matcher fullRefMatch = FULL_USER_REF.matcher(query);

        if (userMention.matches()) {
            User user = catnip.cache().users().getById(userMention.group(1));

            if (user != null) {
                return Collections.singletonList(user);
            }
        } else if (fullRefMatch.matches()) {
            String lowerName = fullRefMatch.group(1).toLowerCase();
            String discrim = fullRefMatch.group(2);
            List<User> users = catnip.cache().users()
                .stream().filter(user -> user.username().toLowerCase().equals(lowerName) && user.discriminator().equals(discrim))
                .collect(Collectors.toList());

            if (!users.isEmpty()) {
                return users;
            }
        } else if (DISCORD_ID.matcher(query).matches()) {
            User user = catnip.cache().users().getById(query);

            if (user != null) {
                return Collections.singletonList(user);
            }
        }

        ArrayList<User> exact = new ArrayList<>();
        ArrayList<User> wrongcase = new ArrayList<>();
        ArrayList<User> startswith = new ArrayList<>();
        ArrayList<User> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        catnip.cache().users().forEach(user -> {
            String name = user.username();

            if (name.equals(query)) {
                exact.add(user);
            } else if (name.equalsIgnoreCase(query) && exact.isEmpty()) {
                wrongcase.add(user);
            } else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty()) {
                startswith.add(user);
            } else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty()) {
                contains.add(user);
            }
        });

        if (!exact.isEmpty()) {
            return Collections.unmodifiableList(exact);
        }
        if (!wrongcase.isEmpty()) {
            return Collections.unmodifiableList(wrongcase);
        }
        if (!startswith.isEmpty()) {
            return Collections.unmodifiableList(startswith);
        }
        return Collections.unmodifiableList(contains);
    }

    public static List<User> findBannedUsers(String query, Guild guild) {
        List<User> bans;

        try {
            bans = guild.catnip().rest().guild().getGuildBans(guild.id())
                .toCompletableFuture().get().stream()
                .map(GuildBan::user)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }

        String discrim = null;
        Matcher userMention = USER_MENTION.matcher(query);

        if (userMention.matches()) {
            String id = userMention.group(1);
            User user = guild.catnip().cache().users().getById(id);

            if (user != null && bans.contains(user)) {
                return Collections.singletonList(user);
            }

            for (User u : bans) {
                if (u.id().equals(id)) {
                    return Collections.singletonList(u);
                }
            }
        } else if (FULL_USER_REF.matcher(query).matches()) {
            discrim = query.substring(query.length() - 4);
            query = query.substring(0, query.length() - 5).trim();
        } else if (DISCORD_ID.matcher(query).matches()) {
            User user = guild.catnip().cache().users().getById(query);

            if (user != null && bans.contains(user)) {
                return Collections.singletonList(user);
            }

            for (User u : bans) {
                if (u.id().equals(query)) {
                    return Collections.singletonList(u);
                }
            }
        }

        ArrayList<User> exact = new ArrayList<>();
        ArrayList<User> wrongcase = new ArrayList<>();
        ArrayList<User> startswith = new ArrayList<>();
        ArrayList<User> contains = new ArrayList<>();

        String lowerQuery = query.toLowerCase();
        for (User u : bans) {
            // If a discrim is specified then we skip all users without it.
            if (discrim != null && !u.discriminator().equals(discrim)) {
                continue;
            }

            if (u.username().equals(query)) {
                exact.add(u);
            } else if (exact.isEmpty() && u.username().equalsIgnoreCase(query)) {
                wrongcase.add(u);
            } else if (wrongcase.isEmpty() && u.username().toLowerCase().startsWith(lowerQuery)) {
                startswith.add(u);
            } else if (startswith.isEmpty() && u.username().toLowerCase().contains(lowerQuery)) {
                contains.add(u);
            }
        }

        if (!exact.isEmpty()) {
            return Collections.unmodifiableList(exact);
        }

        if (!wrongcase.isEmpty()) {
            return Collections.unmodifiableList(wrongcase);
        }

        if (!startswith.isEmpty()) {
            return Collections.unmodifiableList(startswith);
        }

        return Collections.unmodifiableList(contains);
    }

    public static List<Member> findMembers(String query, Guild guild) {
        Matcher userMention = USER_MENTION.matcher(query);
        Matcher fullRefMatch = FULL_USER_REF.matcher(query);

        if (userMention.matches()) {
            Member member = guild.members().getById(userMention.group(1));

            if (member != null) {
                return Collections.singletonList(member);
            }
        } else if (fullRefMatch.matches()) {
            String lowerName = fullRefMatch.group(1).toLowerCase();
            String discrim = fullRefMatch.group(2);
            List<Member> members = guild.members().stream()
                .filter(member -> member.user().username().toLowerCase().equals(lowerName)
                    && member.user().discriminator().equals(discrim))
                .collect(Collectors.toList());

            if (!members.isEmpty()) {
                return members;
            }
        } else if (DISCORD_ID.matcher(query).matches()) {
            Member member = guild.members().getById(query);

            if (member != null) {
                return Collections.singletonList(member);
            }
        }

        ArrayList<Member> exact = new ArrayList<>();
        ArrayList<Member> wrongcase = new ArrayList<>();
        ArrayList<Member> startswith = new ArrayList<>();
        ArrayList<Member> contains = new ArrayList<>();

        String lowerquery = query.toLowerCase();
        guild.members().forEach(member -> {
            String name = member.user().username();
            String effName = member.effectiveName();

            if (name.equals(query) || effName.equals(query)) {
                exact.add(member);
            } else if ((name.equalsIgnoreCase(query) || effName.equalsIgnoreCase(query)) && exact.isEmpty()) {
                wrongcase.add(member);
            } else if ((name.toLowerCase().startsWith(lowerquery) || effName.toLowerCase().startsWith(lowerquery)) && wrongcase.isEmpty()) {
                startswith.add(member);
            } else if ((name.toLowerCase().contains(lowerquery) || effName.toLowerCase().contains(lowerquery)) && startswith.isEmpty()) {
                contains.add(member);
            }
        });

        if (!exact.isEmpty()) {
            return Collections.unmodifiableList(exact);
        }

        if (!wrongcase.isEmpty()) {
            return Collections.unmodifiableList(wrongcase);
        }

        if (!startswith.isEmpty()) {
            return Collections.unmodifiableList(startswith);
        }

        return Collections.unmodifiableList(contains);
    }

    public static List<GuildChannel> findChannels(String query, Catnip catnip) {
        return genericChannelSearch(query, ChannelFilter.ANY, catnip.cache().channels());
    }

    public static List<GuildChannel> findChannels(String query, Guild guild) {
        return genericChannelSearch(query, ChannelFilter.ANY, guild.channels());
    }

    public static List<TextChannel> findTextChannels(String query, Catnip catnip) {
        return genericChannelSearch(query, ChannelFilter.TEXT, catnip.cache().channels());
    }

    public static List<TextChannel> findTextChannels(String query, Guild guild) {
        return genericChannelSearch(query, ChannelFilter.TEXT, guild.channels());
    }

    public static List<VoiceChannel> findVoiceChannels(String query, Catnip catnip) {
        return genericChannelSearch(query, ChannelFilter.VOICE, catnip.cache().channels());
    }

    public static List<VoiceChannel> findVoiceChannels(String query, Guild guild) {
        return genericChannelSearch(query, ChannelFilter.VOICE, guild.channels());
    }

    public static List<Category> findCategories(String query, Catnip catnip) {
        return genericChannelSearch(query, ChannelFilter.CATEGORY, catnip.cache().channels());
    }

    public static List<Category> findCategories(String query, Guild guild) {
        return genericChannelSearch(query, ChannelFilter.CATEGORY, guild.channels());
    }

    public static List<Role> findRoles(String query, Guild guild) {
        Matcher roleMention = ROLE_MENTION.matcher(query);

        if (roleMention.matches()) {
            Role role = guild.roles().getById(roleMention.group(1));

            if (role != null && role.mentionable()) {
                return Collections.singletonList(role);
            }
        } else if (DISCORD_ID.matcher(query).matches()) {
            Role role = guild.roles().getById(query);

            if (role != null) {
                return Collections.singletonList(role);
            }
        }

        ArrayList<Role> exact = new ArrayList<>();
        ArrayList<Role> wrongcase = new ArrayList<>();
        ArrayList<Role> startswith = new ArrayList<>();
        ArrayList<Role> contains = new ArrayList<>();

        String lowerquery = query.toLowerCase();
        guild.roles().forEach((role) -> {
            String name = role.name();

            if (name.equals(query)) {
                exact.add(role);
            } else if (name.equalsIgnoreCase(query) && exact.isEmpty()) {
                wrongcase.add(role);
            } else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty()) {
                startswith.add(role);
            } else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty()) {
                contains.add(role);
            }
        });

        if (!exact.isEmpty()) {
            return Collections.unmodifiableList(exact);
        }

        if (!wrongcase.isEmpty()) {
            return Collections.unmodifiableList(wrongcase);
        }

        if (!startswith.isEmpty()) {
            return Collections.unmodifiableList(startswith);
        }

        return Collections.unmodifiableList(contains);
    }

    public static List<Emoji> findEmojis(String query, Guild guild) {
        return genericEmojiSearch(query, guild.emojis());
    }

    public static List<Emoji> findEmojis(String query, Catnip catnip) {
        return genericEmojiSearch(query, catnip.cache().emojis());
    }

    private static <T extends GuildChannel> List<T> genericChannelSearch(String query, ChannelFilter<T> f, NamedCacheView<GuildChannel> cache) {
        Matcher channelMention = CHANNEL_MENTION.matcher(query);

        if (f.isMentionable() && channelMention.matches()) {
            GuildChannel c = cache.getById(channelMention.group(1));

            if (!f.canCast(c)) {
                return null;
            }

            return Collections.singletonList(f.cast(c));
        } else if (DISCORD_ID.matcher(query).matches()) {
            GuildChannel c = cache.getById(query);

            if (!f.canCast(c)) {
                return null;
            }

            return Collections.singletonList(f.cast(c));
        }

        ArrayList<T> exact = new ArrayList<>();
        ArrayList<T> wrongcase = new ArrayList<>();
        ArrayList<T> startswith = new ArrayList<>();
        ArrayList<T> contains = new ArrayList<>();

        String lowerquery = query.toLowerCase();
        cache.forEach((c) -> {
            if (!f.canCast(c)) {
                return;
            }

            T tc = f.cast(c);
            String name = tc.name();

            if (name.equals(query)) {
                exact.add(tc);
            } else if (name.equalsIgnoreCase(query) && exact.isEmpty()) {
                wrongcase.add(tc);
            } else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty()) {
                startswith.add(tc);
            } else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty()) {
                contains.add(tc);
            }
        });

        if (!exact.isEmpty()) {
            return Collections.unmodifiableList(exact);
        }

        if (!wrongcase.isEmpty()) {
            return Collections.unmodifiableList(wrongcase);
        }

        if (!startswith.isEmpty()) {
            return Collections.unmodifiableList(startswith);
        }

        return Collections.unmodifiableList(contains);
    }

    private static List<Emoji> genericEmojiSearch(String query, NamedCacheView<Emoji.CustomEmoji> cache) {
        Matcher mentionMatcher = EMOTE_MENTION.matcher(query);

        if (DISCORD_ID.matcher(query).matches()) {
            Emoji emoji = cache.getById(query);

            if (emoji != null) {
                return Collections.singletonList(emoji);
            }
        } else if (mentionMatcher.matches()) {
            String emojiName = mentionMatcher.group(1);
            String emojiId = mentionMatcher.group(2);
            Emoji emoji = cache.getById(emojiId);

            if (emoji != null && emoji.name().equals(emojiName)) {
                return Collections.singletonList(emoji);
            }
        }

        ArrayList<Emoji> exact = new ArrayList<>();
        ArrayList<Emoji> wrongcase = new ArrayList<>();
        ArrayList<Emoji> startswith = new ArrayList<>();
        ArrayList<Emoji> contains = new ArrayList<>();

        String lowerquery = query.toLowerCase();
        cache.forEach(emoji -> {
            String name = emoji.name();

            if (name.equals(query)) {
                exact.add(emoji);
            } else if (name.equalsIgnoreCase(query) && exact.isEmpty()) {
                wrongcase.add(emoji);
            } else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty()) {
                startswith.add(emoji);
            } else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty()) {
                contains.add(emoji);
            }
        });

        if (!exact.isEmpty()) {
            return Collections.unmodifiableList(exact);
        }

        if (!wrongcase.isEmpty()) {
            return Collections.unmodifiableList(wrongcase);
        }

        if (!startswith.isEmpty()) {
            return Collections.unmodifiableList(startswith);
        }

        return Collections.unmodifiableList(contains);
    }

    // Prevent instantiation
    private FinderUtil() {
    }

    private interface ChannelFilter<T extends GuildChannel> {
        default boolean isMentionable() {
            return false;
        }

        boolean canCast(GuildChannel channel);

        T cast(GuildChannel channel);

        ChannelFilter<GuildChannel> ANY = new ChannelFilter<GuildChannel>() {
            @Override
            public boolean canCast(GuildChannel channel) {
                return true;
            }

            @Override
            public GuildChannel cast(GuildChannel channel) {
                return channel;
            }
        };

        ChannelFilter<TextChannel> TEXT = new ChannelFilter<TextChannel>() {
            @Override
            public boolean isMentionable() {
                return true;
            }

            @Override
            public boolean canCast(GuildChannel channel) {
                return channel.isText();
            }

            @Override
            public TextChannel cast(GuildChannel channel) {
                return channel.asTextChannel();
            }
        };

        ChannelFilter<VoiceChannel> VOICE = new ChannelFilter<VoiceChannel>() {
            @Override
            public boolean canCast(GuildChannel channel) {
                return channel.isVoice();
            }

            @Override
            public VoiceChannel cast(GuildChannel channel) {
                return channel.asVoiceChannel();
            }
        };

        ChannelFilter<Category> CATEGORY = new ChannelFilter<Category>() {
            @Override
            public boolean canCast(GuildChannel channel) {
                return channel.isCategory();
            }

            @Override
            public Category cast(GuildChannel channel) {
                return channel.asCategory();
            }
        };
    }
}
