package cards.entity;

import lombok.Data;

import java.util.Map;

/**
 * Created by asus on 2016/10/23.
 */
@Data
public class Storage {
    private String success;
    private Map<String,Item> rgInventory;
    private Map<String,LoadType> rgDescriptions;
    private Boolean more;
    private Object more_start;
}
