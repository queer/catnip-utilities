package gg.amy.catnip.utilities.menu.component;

import java.util.List;

/**
 * @author amy
 * @since 10/9/18.
 */
public interface PaginatedMenu extends Menu {
    int pages();
    
    int page();
    
    List<String> pageData();
    
    void nextPage();
    
    void previousPage();
}
