package cards.repository;

import cards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by asus on 2017/1/15.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String>{
}
