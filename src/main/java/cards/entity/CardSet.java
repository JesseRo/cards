package cards.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/11.
 */
@Data
public class CardSet {
    private App app;
    private List<Item> items = new ArrayList<>();
    public boolean add(Item item){
        return items.add(item);
    }
}
