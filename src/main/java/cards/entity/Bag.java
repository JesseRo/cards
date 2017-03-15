package cards.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by asus on 2017/1/21.
 */
@Data
public class Bag {
    List<App > cards;
    List<Integer> numbers;
    List<CardSet> items;
    public Bag(List<CardSet> items){
        this.items = items;
    }
    public static Bag merge(Bag one, Bag another){
        if (one == null && another == null){
            return null;
        }else if (one != null && another == null){
            return one;
        }else if(one == null){
            return another;
        }else {
            one.getItems().addAll(another.getItems());
            return one;
        }
    }
    public List<Item> allItems(){
        List<Item> allItems = new ArrayList<>();
        for (CardSet cardSet : items){
            allItems.addAll(cardSet.getItems());
        }
        return allItems;
    }

    public void remove(List<CardSet> cardSets){
        items.removeAll(cardSets);
    }

    public Bag init() {
        Map<App,List<CardSet>> cardGroup = items.stream().collect(Collectors.groupingBy(CardSet::getApp));
        cards = new ArrayList<>();
        numbers = new ArrayList<>();
        for (Map.Entry<App,List<CardSet>> cardSetEntry : cardGroup.entrySet()){
            cards.add(cardSetEntry.getKey());
            numbers.add(cardSetEntry.getValue().size());
        }
        return this;
    }
}
