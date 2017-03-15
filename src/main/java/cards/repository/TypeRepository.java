package cards.repository;

import cards.entity.App;
import cards.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Created by asus on 2016/11/6.
 */
@Repository
public interface TypeRepository extends JpaRepository<Type,String> {
    @Query("select t from Type as t where t.app.price is not null and t.app not in :appSet order by t.app.price")
    List<Type> findCheapest(@Param("appSet")Collection<App> appSet);

    @Query("select t from Type as t where t.app.price is not null order by t.app.price")
    List<Type> findCheapest();
}
