package cards.entity;


import lombok.Data;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Table;

/**
 * Created by asus on 2017/1/24.
 */
@Data
@javax.persistence.Entity
@Table(name = "trad_history")
public class History {
    @Id
    @GeneratedValue(generator = "uuidGenerator")
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    private String id;

    private String openid;

    @org.hibernate.annotations.Type(type = "text")
    //json
    private String record;

    private String tradeId;

    public History(){

    }

    public History(String openid,String record,String tradeId){
        this.openid = openid;
        this.record = record;
        this.tradeId = tradeId;
    }
}
