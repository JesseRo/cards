package cards.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by asus on 2016/10/30.
 */
@Data
@Entity
@Table(name = "item_type")
public class Type {
    @Id
    private String id;
    private String classid;
    private String instanceid;
    private String market_hash_name;
    private String market_name;
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "market_fee_app")
    private App app;
    @Column(updatable = false, insertable = false)
    private String market_fee_app;

    public Type(){

    }
    public Type(LoadType loadType){
        this.id = loadType.getId();
        this.classid = loadType.getClassid();
        this.instanceid = loadType.getInstanceid();
        this.market_hash_name = loadType.getMarket_hash_name();
        this.market_name = loadType.getMarket_name();
        this.name = loadType.getName();
    }
}
