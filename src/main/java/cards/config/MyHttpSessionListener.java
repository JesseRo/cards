package cards.config;

import cards.entity.CardSet;
import cards.entity.Item;
import cards.entity.json.CardPickedResult;
import cards.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2017/1/21.
 */
@WebListener
@Component
public class MyHttpSessionListener implements HttpSessionListener {
    @Autowired
    private ItemRepository itemRepository;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        se.getSession().setMaxInactiveInterval(10 * 60);
        System.out.println("Session 被创建");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        if (se.getSession().getAttribute("unsubmittedOrder") != null) {
            CardPickedResult result = (CardPickedResult)se.getSession().getAttribute("unsubmittedOrder");
            List<Item> itemList = new ArrayList<>();
            for (CardSet cardSet : result.getItems()){
                cardSet.getItems().forEach(p->p.setPlaced(false));
                itemList.addAll(cardSet.getItems());
            }
            itemRepository.save(itemList);
        }
        System.out.println("Session 被释放");
    }

}