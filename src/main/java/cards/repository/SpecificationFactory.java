package cards.repository;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * Created by Administrator on 2016/3/21.
 */
public class SpecificationFactory {


    public static <T,S>Specification<T> getEqual(final String propertyName, final S value){
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.equal(root.<S>get(propertyName), value);
            }
        };
    }

    public static <T,S extends Comparable>Specification<T> getBetween(final String propertyName, final S s,final S e){
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.between(root.<S>get(propertyName), s, e);
            }
        };
    }

    public static <T,S extends Comparable>Specification<T> getLessThan(final String propertyName, final S s){
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.lessThan(root.<S>get(propertyName), s);
            }
        };
    }


    public static <T,S extends Comparable>Specification<T> getGreaterThan(final String propertyName, final S s){
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.greaterThan(root.<S>get(propertyName), s);
            }
        };
    }

    public static <T>Specification<T> getLike(final String propertyName,final String value){
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return cb.like(root.<String>get(propertyName), value);
            }
        };
    }
    public static <T,S>Specification<T> getIn(final String propertyName,final Collection<S> value){
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.<S>get(propertyName).in(value);
            }
        };
    }
}
