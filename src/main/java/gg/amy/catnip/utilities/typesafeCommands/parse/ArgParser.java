package gg.amy.catnip.utilities.typesafeCommands.parse;

import com.mewna.catnip.entity.message.Message;
import gg.amy.catnip.utilities.typesafeCommands.TypesafeCommandExtension;
import gg.amy.catnip.utilities.typesafeCommands.TypesafeCommandExtension.CommandContainer;

import java.util.Map;

/**
 * @author amy
 * @since 10/8/18.
 */
public interface ArgParser {
    Map<String, Object> parse(TypesafeCommandExtension extension, CommandContainer cmd, Message ctx, String argstr);
}
