package cards.repository;

import cards.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by asus on 2017/1/24.
 */
@Repository
public interface HistoryRepository extends JpaRepository<History,String>{

}
