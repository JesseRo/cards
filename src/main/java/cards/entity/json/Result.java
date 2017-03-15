package cards.entity.json;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by asus on 2016/11/12.
 */
@Data
@AllArgsConstructor
public class Result {
    public static final Integer EMPTY_BAG_EXCEPTION = 4;
    public static final Integer TRADE_URL_EXCEPTION = 5;
    public static final Integer NOT_AFFORDABLE_EXCEPTION = 6;
    public static Integer NETWORK_EXCEPTION = 1;
    public static Integer USER_LEVEL_EXCEPTION = 2;
    public static Integer AUTH_EXCEPTION = 3;

    private boolean result;
    private String message;
    private Object data;
    public static Result Success(Object o){
        return new Result(true, "", o);
    }
    public static Result Failure(String message, Integer code){
        return new Result(false, message, code);
    }
}
