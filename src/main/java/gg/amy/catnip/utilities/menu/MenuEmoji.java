package gg.amy.catnip.utilities.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 10/8/18.
 */
@Getter
@Builder
@Accessors(fluent = true)
@SuppressWarnings({"WeakerAccess", "FieldMayBeFinal"})
@NoArgsConstructor
@AllArgsConstructor
public class MenuEmoji {
    private String leftArrow = "⬅";
    private String rightArrow = "➡";
    private String accept = "✅";
    private String cancel = "\uD83D\uDEAB";
    private String refresh = "\uD83D\uDD04";
}
