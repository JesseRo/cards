package cards.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by asus on 2016/12/25.
 */
@Data
@Entity
@Table(name = "applist")
public class App {
    @Id
    @Column(name = "appid")
    private String appId;
    private String name;
    private Float price;
    private Integer number = 0;
}
