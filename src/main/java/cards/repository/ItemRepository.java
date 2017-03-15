package cards.repository;

/**
 * Created by asus on 2016/11/6.
 */

import cards.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item,String>,JpaSpecificationExecutor<Item> {
    @Query("select count(item.id) from Item as item where item.placed = false and item.classid in :classid group by item.classid")
    List<Long> countByClassidIn(@Param("classid")Collection<String> classid);
}