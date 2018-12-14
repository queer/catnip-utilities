package gg.amy.catnip.utilities.command.parse;

import com.mewna.catnip.entity.message.Message;
import gg.amy.zoe.ext.command.CommandExtension;
import gg.amy.zoe.ext.command.CommandExtension.CommandContainer;

import java.util.Map;

/**
 * @author amy
 * @since 10/8/18.
 */
public interface ArgParser {
    Map<String, Object> parse(CommandExtension extension, CommandContainer cmd, Message ctx, String argstr);
}
