package cards.entity;

import cards.entity.rest.profile.Player;
import lombok.Data;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by asus on 2017/1/8.
 */
@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    private String id;

    private Float money = 0f;

    private String name;

    private Integer level = 0;

    private String tradeUrl;

    private String token;

    private Integer exp = 0;

    @org.hibernate.annotations.Type(type = "text")
    private String bag;

}
