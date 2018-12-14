package gg.amy.catnip.utilities.typesafeCommands.parse.impl;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static gg.amy.catnip.utilities.typesafeCommands.parse.impl.UnixArgParser.NOFLAG;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author amy
 * @since 12/14/18.
 */
class UnixArgParserTest {
    @Test
    void parseFlags() {
        final UnixArgParser parser = new UnixArgParser();
        {
            final Map<String, List<String>> flags = parser.parseFlags("a b c --asdf --flag \"this is a whitespaced token\" " +
                    "--thing \"This is a whitespaced token with \\\"embedded\\\" quotes~\" " +
                    "--flag multi --flag flag --flag args --flag yay");
            assertEquals(ImmutableList.of(), flags.get("--asdf"));
            assertEquals(ImmutableList.of("a", "b", "c"), flags.get(NOFLAG));
            assertEquals(ImmutableList.of("\"this is a whitespaced token\"", "multi", "flag", "args", "yay"), flags.get("--flag"));
            assertEquals(ImmutableList.of("\"This is a whitespaced token with \\\"embedded\\\" quotes~\""), flags.get("--thing"));
            
        }
        {
            final Map<String, List<String>> flags = parser.parseFlags("a b c --asdf --flag");
            assertEquals(ImmutableList.of(), flags.get("--flag"));
            assertEquals(ImmutableList.of(), flags.get("--asdf"));
            assertEquals(ImmutableList.of("a", "b", "c"), flags.get(NOFLAG));
        }
        {
            final Map<String, List<String>> flags = parser.parseFlags("--asdf a b c --flag");
            assertEquals(ImmutableList.of(), flags.get("--flag"));
            assertEquals(ImmutableList.of("a"), flags.get("--asdf"));
            assertEquals(ImmutableList.of("b", "c"), flags.get(NOFLAG));
        }
    }
}