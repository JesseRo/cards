package cards.service;

import cards.entity.User;
import cards.entity.json.CardPickedResult;
import cards.entity.json.Result;
import cards.entity.rest.ChargeResult;

import javax.transaction.Transactional;

/**
 * Created by asus on 2017/1/8.
 */
public interface BusinessService {

    @Transactional
    CardPickedResult upLevel(String openId, Integer targetLevel) throws Exception;

    @Transactional
    Integer maxLevel(String openId) throws Exception;

    @Transactional
    boolean buy(String openId);

    @Transactional
    Result trade(String openId);


    User info(String openId) throws Exception;

    void saveTradUrl(String openId,String url);

    ChargeResult charge(String openId, String oid) throws Exception;
}
