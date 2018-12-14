package gg.amy.catnip.utilities.typesafeCommands.types;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author amy
 * @since 9/10/18.
 */
@SuppressWarnings("unused")
@FunctionalInterface
public interface TypeConverter<C, T> {
    Pair<T, String> convert(C context, String input);
    
    static TypeConverter<?, String> identity() {
        return (__, input) -> ImmutablePair.of(input, input);
    }
    
    static TypeConverter<?, String> nullifier() {
        return (__, input) -> ImmutablePair.of(null, input);
    }
}
