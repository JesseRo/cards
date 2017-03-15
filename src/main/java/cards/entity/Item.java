package cards.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by asus on 2016/10/23.
 */
@Data
@Entity
@Table(name = "item")
public class Item {
    @Id
    private String id;
    private String classid;
    private String instanceid;
    private Integer amount;
    private String pos;
    private boolean placed = false;
    private String userId;
}
