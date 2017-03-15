package cards.entity;

import lombok.Data;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by asus on 2016/12/25.
 */
@Data
@Entity
@Table(name = "singlecardinfo")
public class CardInfo {
    @Id
    private int id;
    private String name;
    @JoinColumn(name = "gameid")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    private App app;
    @Column(name = "imgurl")
    @org.hibernate.annotations.Type(type = "text")
    private String imgUrl;
    @org.hibernate.annotations.Type(type = "text")
    private String descriptions;
}
