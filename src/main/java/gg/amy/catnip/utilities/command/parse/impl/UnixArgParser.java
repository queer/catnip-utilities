package gg.amy.catnip.utilities.command.parse.impl;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.message.Message;
import gg.amy.zoe.ext.command.CommandExtension;
import gg.amy.zoe.ext.command.CommandExtension.CommandContainer;
import gg.amy.zoe.ext.command.CommandExtension.ParamTypeInfo;
import gg.amy.zoe.ext.command.parse.ArgParser;
import gg.amy.zoe.ext.command.types.TypeConverter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;

/**
 * UNIX-style CLI arg parsing.
 *
 * @author amy
 * @since 10/8/18.
 */
public class UnixArgParser implements ArgParser {
    static final String NOFLAG = "NOFLAG";
    
    @Override
    public Map<String, Object> parse(final CommandExtension extension, final CommandContainer cmd, final Message ctx,
                                     final String argstr) {
        if(argstr == null || argstr.trim().isEmpty()) {
            return ImmutableMap.of();
        }
        final Map<String, Object> result = new LinkedHashMap<>();
        
        final Map<String, List<String>> flags = parseFlags(argstr);
        for(final Entry<String, List<String>> entry : flags.entrySet()) {
            if(entry.getKey().startsWith("--")) {
                final String arg = entry.getKey().replaceFirst("--", "");
                final ParamTypeInfo typeInfo = cmd.typeInfo().get(arg);
                final List<String> values = entry.getValue();
                if(values.size() > 1 && !Collection.class.isAssignableFrom(typeInfo.typeClass())) {
                    ctx.catnip().logAdapter().warn("Command {} passed {} args for flag {}, but only 1 was expected! Only taking first arg!",
                            cmd.name(), values.size(), arg);
                }
                final Object res;
                if(values.isEmpty()) {
                    res = tryConvert(extension, ctx, "true", typeInfo.typeClass()).getLeft();
                } else {
                    res = tryConvert(extension, ctx, values.get(0), typeInfo.typeClass()).getLeft();
                }
                result.put(arg, res);
            } else if(entry.getKey().equals(NOFLAG)) {
                // TODO: How to handle this?
                if(!entry.getValue().isEmpty()) {
                    ctx.catnip().logAdapter().warn("NOFLAG ({}) args: {}", NOFLAG, entry.getValue());
                }
            } else {
                ctx.catnip().logAdapter().warn("Flag {} doesn't start with -- and isn't NOFLAG ({})!", entry.getKey(), NOFLAG);
            }
        }
        
        return Collections.unmodifiableMap(result);
    }
    
    Map<String, List<String>> parseFlags(final String argstr) {
        // Tokenize
        final Collection<String> tokens = new ArrayList<>();
        {
            char last = '\0';
            StringBuilder token = new StringBuilder();
            boolean insideQuotes = false;
            
            for(final char c : argstr.trim().toCharArray()) {
                if(Character.isWhitespace(c) && !insideQuotes) {
                    // If it's whitespace and we AREN'T inside quotes, then we've
                    // hit the end of the token and need to append it
                    if(!token.toString().trim().isEmpty()) {
                        tokens.add(token.toString());
                    }
                    token = new StringBuilder();
                } else if(c == '"') {
                    // If we hit a quote, determine if we need to start or stop
                    // tokenizing
                    if(insideQuotes) {
                        if(last == '\\') {
                            // If inside quotes, escape if and only if the previous
                            // char was a backslash
                            token.append(c);
                        } else {
                            // Otherwise, append the char and add the token to the
                            // list
                            insideQuotes = false;
                            tokens.add(token.append(c).toString());
                            token = new StringBuilder();
                        }
                    } else if(last != '\\') {
                        // If last character is a \, it's an escape and doesn't
                        // count
                        insideQuotes = true;
                        token.append(c);
                    }
                } else {
                    // Probably not special, just append
                    token.append(c);
                }
                last = c;
            }
            // Last token may be missing, so collect it
            if(token.length() != 0) {
                tokens.add(token.toString());
            }
        }
        final Map<String, List<String>> flagArgs = new HashMap<>();
        {
            final List<String> noFlagArgs = new ArrayList<>();
            String lastFlag = null;
            for(final String next : tokens) {
                if(next.startsWith("-")) {
                    lastFlag = next;
                    flagArgs.putIfAbsent(next, new ArrayList<>());
                } else {
                    if(lastFlag == null) {
                        noFlagArgs.add(next);
                    } else {
                        flagArgs.get(lastFlag).add(next);
                        lastFlag = null;
                    }
                }
            }
            flagArgs.put(NOFLAG, noFlagArgs);
        }
        
        return ImmutableMap.copyOf(flagArgs);
    }
    
    // This is type-safe...
    // ...probably...
    @SuppressWarnings("unchecked")
    private <T> Pair<T, String> tryConvert(final CommandExtension extension, final Message message, final String argstr,
                                           final Class<T> cls) {
        final TypeConverter<Message, T> conv = (TypeConverter<Message, T>) extension.converters().getOrDefault(cls,
                (TypeConverter<Message, T>) TypeConverter.nullifier());
        return conv.convert(message, argstr);
    }
}
