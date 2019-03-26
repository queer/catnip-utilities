package gg.amy.catnip.utilities;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.cache.view.NamedCacheView;
import com.mewna.catnip.entity.Snowflake;
import com.mewna.catnip.entity.channel.Category;
import com.mewna.catnip.entity.channel.GuildChannel;
import com.mewna.catnip.entity.channel.TextChannel;
import com.mewna.catnip.entity.channel.VoiceChannel;
import com.mewna.catnip.entity.guild.Guild;
import com.mewna.catnip.entity.guild.GuildBan;
import com.mewna.catnip.entity.guild.Member;
import com.mewna.catnip.entity.guild.Role;
import com.mewna.catnip.entity.misc.Emoji;
import com.mewna.catnip.entity.misc.Emoji.CustomEmoji;
import com.mewna.catnip.entity.user.User;

import java.util.*;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author AdrianTodt
 * @since 02/08/19.
 */
@SuppressWarnings({"Duplicates", "unused", "WeakerAccess"})
public final class FinderUtil {
    public static final Pattern DISCORD_ID = Pattern.compile("\\d{1,20}"); // ID
    public static final Pattern FULL_USER_REF = Pattern.compile("(\\S.{0,30}\\S)\\s*#(\\d{4})"); // $1 -> username, $2 -> discriminator
    public static final Pattern USER_MENTION = Pattern.compile("<@!?(\\d{1,20})>"); // $1 -> ID
    public static final Pattern CHANNEL_MENTION = Pattern.compile("<#(\\d{1,20})>"); // $1 -> ID
    public static final Pattern ROLE_MENTION = Pattern.compile("<@&(\\d{1,20})>"); // $1 -> ID
    public static final Pattern EMOTE_MENTION = Pattern.compile("<:(.{2,32}):(\\d{1,20})>");

    public static Collection<User> findUsers(final String query, final Catnip catnip) {
        final Matcher userMention = USER_MENTION.matcher(query);
        final Matcher fullRefMatch = FULL_USER_REF.matcher(query);

        if(userMention.matches()) {
            final User user = catnip.cache().users().getById(userMention.group(1));

            if(user != null) {
                return Collections.singleton(user);
            }
        } else if(fullRefMatch.matches()) {
            final String lowerName = fullRefMatch.group(1).toLowerCase();
            final String discrim = fullRefMatch.group(2);
            final Collection<User> users = catnip.cache().users()
                .find(user -> user.username().toLowerCase().equals(lowerName) && user.discriminator().equals(discrim));

            if(!users.isEmpty()) {
                return users;
            }
        } else if(DISCORD_ID.matcher(query).matches()) {
            final User user = catnip.cache().users().getById(query);

            if(user != null) {
                return Collections.singleton(user);
            }
        }

        final Collection<User> exact = new ArrayList<>();
        final Collection<User> wrongcase = new ArrayList<>();
        final Collection<User> startswith = new ArrayList<>();
        final Collection<User> contains = new ArrayList<>();
        final String lowerquery = query.toLowerCase();
        catnip.cache().users().forEach(user -> {
            final String name = user.username();

            if(name.equals(query)) {
                exact.add(user);
            } else if(name.equalsIgnoreCase(query) && exact.isEmpty()) {
                wrongcase.add(user);
            } else if(name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty()) {
                startswith.add(user);
            } else if(name.toLowerCase().contains(lowerquery) && startswith.isEmpty()) {
                contains.add(user);
            }
        });

        if(!exact.isEmpty()) {
            return Collections.unmodifiableCollection(exact);
        }

        if(!wrongcase.isEmpty()) {
            return Collections.unmodifiableCollection(wrongcase);
        }

        if(!startswith.isEmpty()) {
            return Collections.unmodifiableCollection(startswith);
        }
        
        return Collections.unmodifiableCollection(contains);
    }

    public static Collection<User> findBannedUsers(String query, final Snowflake guild) {

        final Collection<User> bans = guild.catnip().rest().guild().getGuildBans(guild.id())
            .thenApply(u -> u.stream()
                .map(GuildBan::user)
                .collect(Collectors.toSet())
            ).toCompletableFuture().join();


        String discrim = null;
        final Matcher userMention = USER_MENTION.matcher(query);

        if(userMention.matches()) {
            final String id = userMention.group(1);
            final User user = guild.catnip().cache().users().getById(id);

            if(user != null && bans.contains(user)) {
                return Collections.singleton(user);
            }

            for (final User u : bans) {
                if(u.id().equals(id)) {
                    return Collections.singleton(u);
                }
            }
        } else if(FULL_USER_REF.matcher(query).matches()) {
            discrim = query.substring(query.length() - 4);
            query = query.substring(0, query.length() - 5).trim();
        } else if(DISCORD_ID.matcher(query).matches()) {
            final User user = guild.catnip().cache().users().getById(query);

            if(user != null && bans.contains(user)) {
                return Collections.singleton(user);
            }

            for (final User u : bans) {
                if(u.id().equals(query)) {
                    return Collections.singleton(u);
                }
            }
        }

        final Collection<User> exact = new ArrayList<>();
        final Collection<User> wrongcase = new ArrayList<>();
        final Collection<User> startswith = new ArrayList<>();
        final Collection<User> contains = new ArrayList<>();

        final String lowerQuery = query.toLowerCase();
        for (final User u : bans) {
            // ifa discrim is specified then we skip all users without it.
            if(discrim != null && !u.discriminator().equals(discrim)) {
                continue;
            }

            if(u.username().equals(query)) {
                exact.add(u);
            } else if(exact.isEmpty() && u.username().equalsIgnoreCase(query)) {
                wrongcase.add(u);
            } else if(wrongcase.isEmpty() && u.username().toLowerCase().startsWith(lowerQuery)) {
                startswith.add(u);
            } else if(startswith.isEmpty() && u.username().toLowerCase().contains(lowerQuery)) {
                contains.add(u);
            }
        }

        if(!exact.isEmpty()) {
            return Collections.unmodifiableCollection(exact);
        }

        if(!wrongcase.isEmpty()) {
            return Collections.unmodifiableCollection(wrongcase);
        }

        if(!startswith.isEmpty()) {
            return Collections.unmodifiableCollection(startswith);
        }

        return Collections.unmodifiableCollection(contains);
    }

    public static Collection<Member> findMembers(final String query, final Guild guild) {
        final Matcher userMention = USER_MENTION.matcher(query);
        final Matcher fullRefMatch = FULL_USER_REF.matcher(query);

        if(userMention.matches()) {
            final Member member = guild.members().getById(userMention.group(1));

            if(member != null) {
                return Collections.singleton(member);
            }
        } else if(fullRefMatch.matches()) {
            final String lowerName = fullRefMatch.group(1).toLowerCase();
            final String discrim = fullRefMatch.group(2);
            final Collection<Member> members = guild.members()
                .find(member -> member.user().username().toLowerCase().equals(lowerName)
                    && member.user().discriminator().equals(discrim));

            if(!members.isEmpty()) {
                return members;
            }
        } else if(DISCORD_ID.matcher(query).matches()) {
            final Member member = guild.members().getById(query);

            if(member != null) {
                return Collections.singleton(member);
            }
        }

        final Collection<Member> exact = new ArrayList<>();
        final Collection<Member> wrongcase = new ArrayList<>();
        final Collection<Member> startswith = new ArrayList<>();
        final Collection<Member> contains = new ArrayList<>();

        final String lowerquery = query.toLowerCase();
        guild.members().forEach(member -> {
            final String name = member.user().username();
            final String effName = member.effectiveName();

            if(name.equals(query) || effName.equals(query)) {
                exact.add(member);
            } else if((name.equalsIgnoreCase(query) || effName.equalsIgnoreCase(query)) && exact.isEmpty()) {
                wrongcase.add(member);
            } else if((name.toLowerCase().startsWith(lowerquery) || effName.toLowerCase().startsWith(lowerquery)) && wrongcase.isEmpty()) {
                startswith.add(member);
            } else if((name.toLowerCase().contains(lowerquery) || effName.toLowerCase().contains(lowerquery)) && startswith.isEmpty()) {
                contains.add(member);
            }
        });

        if(!exact.isEmpty()) {
            return Collections.unmodifiableCollection(exact);
        }

        if(!wrongcase.isEmpty()) {
            return Collections.unmodifiableCollection(wrongcase);
        }

        if(!startswith.isEmpty()) {
            return Collections.unmodifiableCollection(startswith);
        }

        return Collections.unmodifiableCollection(contains);
    }

    public static Collection<GuildChannel> findChannels(final String query, final Catnip catnip) {
        return genericChannelSearch(query, ChannelFilter.ANY, catnip.cache().channels());
    }

    public static Collection<GuildChannel> findChannels(final String query, final Guild guild) {
        return genericChannelSearch(query, ChannelFilter.ANY, guild.channels());
    }

    public static Collection<TextChannel> findTextChannels(final String query, final Catnip catnip) {
        return genericChannelSearch(query, ChannelFilter.TEXT, catnip.cache().channels());
    }

    public static Collection<TextChannel> findTextChannels(final String query, final Guild guild) {
        return genericChannelSearch(query, ChannelFilter.TEXT, guild.channels());
    }

    public static Collection<VoiceChannel> findVoiceChannels(final String query, final Catnip catnip) {
        return genericChannelSearch(query, ChannelFilter.VOICE, catnip.cache().channels());
    }

    public static Collection<VoiceChannel> findVoiceChannels(final String query, final Guild guild) {
        return genericChannelSearch(query, ChannelFilter.VOICE, guild.channels());
    }

    public static Collection<Category> findCategories(final String query, final Catnip catnip) {
        return genericChannelSearch(query, ChannelFilter.CATEGORY, catnip.cache().channels());
    }

    public static Collection<Category> findCategories(final String query, final Guild guild) {
        return genericChannelSearch(query, ChannelFilter.CATEGORY, guild.channels());
    }

    public static Collection<Role> findRoles(final String query, final Guild guild) {
        final Matcher roleMention = ROLE_MENTION.matcher(query);

        if(roleMention.matches()) {
            final Role role = guild.roles().getById(roleMention.group(1));

            if(role != null && role.mentionable()) {
                return Collections.singleton(role);
            }
        } else if(DISCORD_ID.matcher(query).matches()) {
            final Role role = guild.roles().getById(query);

            if(role != null) {
                return Collections.singleton(role);
            }
        }

        final Collection<Role> exact = new ArrayList<>();
        final Collection<Role> wrongcase = new ArrayList<>();
        final Collection<Role> startswith = new ArrayList<>();
        final Collection<Role> contains = new ArrayList<>();

        final String lowerquery = query.toLowerCase();
        guild.roles().forEach(role -> {
            final String name = role.name();

            if(name.equals(query)) {
                exact.add(role);
            } else if(name.equalsIgnoreCase(query) && exact.isEmpty()) {
                wrongcase.add(role);
            } else if(name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty()) {
                startswith.add(role);
            } else if(name.toLowerCase().contains(lowerquery) && startswith.isEmpty()) {
                contains.add(role);
            }
        });

        if(!exact.isEmpty()) {
            return Collections.unmodifiableCollection(exact);
        }

        if(!wrongcase.isEmpty()) {
            return Collections.unmodifiableCollection(wrongcase);
        }

        if(!startswith.isEmpty()) {
            return Collections.unmodifiableCollection(startswith);
        }

        return Collections.unmodifiableCollection(contains);
    }

    public static Collection<Emoji> findEmojis(final String query, final Guild guild) {
        return genericEmojiSearch(query, guild.emojis());
    }

    public static Collection<Emoji> findEmojis(final String query, final Catnip catnip) {
        return genericEmojiSearch(query, catnip.cache().emojis());
    }

    private static <T extends GuildChannel> Collection<T> genericChannelSearch(final String query, final ChannelFilter<T> f, final NamedCacheView<GuildChannel> cache) {
        final Matcher channelMention = CHANNEL_MENTION.matcher(query);

        if(f.isMentionable() && channelMention.matches()) {
            final GuildChannel c = cache.getById(channelMention.group(1));

            if(!f.canCast(c)) {
                return null;
            }

            return Collections.singleton(f.cast(c));
        } else if(DISCORD_ID.matcher(query).matches()) {
            final GuildChannel c = cache.getById(query);

            if(!f.canCast(c)) {
                return null;
            }

            return Collections.singleton(f.cast(c));
        }

        final Collection<T> exact = new ArrayList<>();
        final Collection<T> wrongcase = new ArrayList<>();
        final Collection<T> startswith = new ArrayList<>();
        final Collection<T> contains = new ArrayList<>();

        final String lowerquery = query.toLowerCase();
        cache.forEach(c -> {
            if(!f.canCast(c)) {
                return;
            }

            final T tc = f.cast(c);
            final String name = tc.name();

            if(name.equals(query)) {
                exact.add(tc);
            } else if(name.equalsIgnoreCase(query) && exact.isEmpty()) {
                wrongcase.add(tc);
            } else if(name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty()) {
                startswith.add(tc);
            } else if(name.toLowerCase().contains(lowerquery) && startswith.isEmpty()) {
                contains.add(tc);
            }
        });

        if(!exact.isEmpty()) {
            return Collections.unmodifiableCollection(exact);
        }

        if(!wrongcase.isEmpty()) {
            return Collections.unmodifiableCollection(wrongcase);
        }

        if(!startswith.isEmpty()) {
            return Collections.unmodifiableCollection(startswith);
        }

        return Collections.unmodifiableCollection(contains);
    }

    private static Collection<Emoji> genericEmojiSearch(final String query, final NamedCacheView<CustomEmoji> cache) {
        final Matcher mentionMatcher = EMOTE_MENTION.matcher(query);

        if(DISCORD_ID.matcher(query).matches()) {
            final Emoji emoji = cache.getById(query);

            if(emoji != null) {
                return Collections.singleton(emoji);
            }
        } else if(mentionMatcher.matches()) {
            final String emojiName = mentionMatcher.group(1);
            final String emojiId = mentionMatcher.group(2);
            final Emoji emoji = cache.getById(emojiId);

            if(emoji != null && emoji.name().equals(emojiName)) {
                return Collections.singleton(emoji);
            }
        }

        final Collection<Emoji> exact = new ArrayList<>();
        final Collection<Emoji> wrongcase = new ArrayList<>();
        final Collection<Emoji> startswith = new ArrayList<>();
        final Collection<Emoji> contains = new ArrayList<>();

        final String lowerquery = query.toLowerCase();
        cache.forEach(emoji -> {
            final String name = emoji.name();

            if(name.equals(query)) {
                exact.add(emoji);
            } else if(name.equalsIgnoreCase(query) && exact.isEmpty()) {
                wrongcase.add(emoji);
            } else if(name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty()) {
                startswith.add(emoji);
            } else if(name.toLowerCase().contains(lowerquery) && startswith.isEmpty()) {
                contains.add(emoji);
            }
        });

        if(!exact.isEmpty()) {
            return Collections.unmodifiableCollection(exact);
        }

        if(!wrongcase.isEmpty()) {
            return Collections.unmodifiableCollection(wrongcase);
        }

        if(!startswith.isEmpty()) {
            return Collections.unmodifiableCollection(startswith);
        }

        return Collections.unmodifiableCollection(contains);
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
            public boolean canCast(final GuildChannel channel) {
                return true;
            }

            @Override
            public GuildChannel cast(final GuildChannel channel) {
                return channel;
            }
        };

        ChannelFilter<TextChannel> TEXT = new ChannelFilter<TextChannel>() {
            @Override
            public boolean isMentionable() {
                return true;
            }

            @Override
            public boolean canCast(final GuildChannel channel) {
                return channel.isText();
            }

            @Override
            public TextChannel cast(final GuildChannel channel) {
                return channel.asTextChannel();
            }
        };

        ChannelFilter<VoiceChannel> VOICE = new ChannelFilter<VoiceChannel>() {
            @Override
            public boolean canCast(final GuildChannel channel) {
                return channel.isVoice();
            }

            @Override
            public VoiceChannel cast(final GuildChannel channel) {
                return channel.asVoiceChannel();
            }
        };

        ChannelFilter<Category> CATEGORY = new ChannelFilter<Category>() {
            @Override
            public boolean canCast(final GuildChannel channel) {
                return channel.isCategory();
            }

            @Override
            public Category cast(final GuildChannel channel) {
                return channel.asCategory();
            }
        };
    }
}
