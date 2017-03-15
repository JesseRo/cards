package cards.entity.rest;

import lombok.Data;

/**
 * Created by asus on 2016/11/23.
 */
@Data
public class Friend {
    private String steamid;
    private String relationship;
    private String friend_since;
}
