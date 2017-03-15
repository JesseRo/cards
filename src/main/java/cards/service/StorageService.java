package cards.service;

import cards.entity.Item;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by asus on 2016/10/23.
 */
@Service
public interface StorageService {
    List<Item> getAllItems() throws InterruptedException;

    void reload() throws InterruptedException;

    @Transactional
    void getAllItemFromFile();

    void statistics();
}
