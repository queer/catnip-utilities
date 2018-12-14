package gg.amy.catnip.utilities.typesafeCommands.parse.impl;

import com.google.common.collect.ImmutableMap;
import com.mewna.catnip.entity.message.Message;
import gg.amy.catnip.utilities.typesafeCommands.Context;
import gg.amy.catnip.utilities.typesafeCommands.TypesafeCommandExtension;
import gg.amy.catnip.utilities.typesafeCommands.TypesafeCommandExtension.CommandContainer;
import gg.amy.catnip.utilities.typesafeCommands.TypesafeCommandExtension.ParamTypeInfo;
import gg.amy.catnip.utilities.typesafeCommands.parse.ArgParser;
import gg.amy.catnip.utilities.typesafeCommands.types.TypeConverter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author amy
 * @since 10/8/18.
 */
public class GenericArgParser implements ArgParser {
    @Override
    public Map<String, Object> parse(final TypesafeCommandExtension extension, final CommandContainer cmd, final Message ctx,
                                     String argstr) {
        if(argstr == null || argstr.trim().isEmpty()) {
            return ImmutableMap.of();
        }
        final Map<String, Object> parse = new LinkedHashMap<>();
    
        final Map<String, ParamTypeInfo> typeInfo = cmd.typeInfo();
    
        for(final Entry<String, ParamTypeInfo> entry : typeInfo.entrySet()) {
            final String k = entry.getKey();
            final ParamTypeInfo v = entry.getValue();
        
            final Class<?> cls = v.typeClass();
            if(!cls.equals(Context.class)) {
                final Pair<?, String> res = tryConvert(extension, ctx, argstr, cls);
                parse.put(k, res.getLeft());
                argstr = res.getRight().trim();
            }
        }
        
        return ImmutableMap.copyOf(parse);
    }
    
    // This is type-safe...
    // ...probably...
    @SuppressWarnings("unchecked")
    private <T> Pair<T, String> tryConvert(final TypesafeCommandExtension extension, final Message message, final String argstr,
                                           final Class<T> cls) {
        final TypeConverter<Message, T> conv = (TypeConverter<Message, T>) extension.converters().getOrDefault(cls,
                (TypeConverter<Message, T>) TypeConverter.nullifier());
        return conv.convert(message, argstr);
    }
}
