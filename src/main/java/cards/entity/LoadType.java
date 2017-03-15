package cards.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by asus on 2017/1/25.
 */
@Data
public class LoadType {
    @Id
    private String id;
    private String classid;
    private String instanceid;
    private String market_hash_name;
    private String market_name;
    private String name;
    private String market_fee_app;
}
