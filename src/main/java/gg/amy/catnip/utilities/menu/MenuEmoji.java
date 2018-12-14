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
    private String accept = "yes:437708741798920229";
    private String cancel = "xmark:392356116102774786";
    private String refresh = "\uD83D\uDD04";
}
