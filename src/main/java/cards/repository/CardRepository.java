package cards.repository;

import cards.entity.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by asus on 2017/1/21.
 */
@Repository
public interface CardRepository extends JpaRepository<CardInfo,Integer> {
}
