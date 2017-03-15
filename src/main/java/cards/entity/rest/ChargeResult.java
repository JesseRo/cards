package cards.entity.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by asus on 2016/11/12.
 */
@Data
@AllArgsConstructor
public class ChargeResult {
    private boolean result;
    private String message;
    private Float payment;
    public ChargeResult(){

    }
}
