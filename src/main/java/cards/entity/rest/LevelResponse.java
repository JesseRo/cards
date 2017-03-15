package cards.entity.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

/**
 * Created by asus on 2017/1/8.
 */
@Data
public class LevelResponse {
    @JsonIgnore
    private List<GameLevel> badges;
    private Integer player_xp;
    private Integer player_level;
    private Integer player_xp_needed_to_level_up;
    private Integer player_xp_needed_current_level;
}
