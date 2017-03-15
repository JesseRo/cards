package cards.entity.json;

import cards.entity.App;
import cards.entity.CardInfo;
import cards.entity.CardSet;
import cards.entity.Item;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by asus on 2017/1/15.
 */
@Data
@AllArgsConstructor
public class CardPickedResult {
    private Integer level;
    List<App> cards;
    List<Integer> numbers;
    Float billMoney;
    Float totalMoney;
    List<CardSet> items;
    public CardPickedResult(){

    }
    public List<Item> allItems(){
        List<Item> allItems = new ArrayList<>();
        for (CardSet cardSet : items){
            allItems.addAll(cardSet.getItems());
        }
        return allItems;
    }
}
