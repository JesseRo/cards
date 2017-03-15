package cards.util;

import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by asus on 2017/1/8.
 */
public class Utils {
    private static RestTemplate restTemplate = new RestTemplate();
    public static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toLowerCase());
        }
        return sign.toString();
    }
    //Md5摘要
    public static byte[] encryptMD5(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        return md5.digest(data.getBytes("UTF-8"));
    }

    public static int levelUp(int currentLevel,int numberOfPairs) throws Exception {
        if (currentLevel < 0){
            throw new Exception("current level not valid");
        }
        int tenTh = currentLevel / 10 + 1;
        int extra = currentLevel % 10;
        if(numberOfPairs > tenTh * (10 - extra)){
            return levelUp(tenTh * 10, numberOfPairs - tenTh * (10 - extra));
        }else {
            return currentLevel + numberOfPairs / tenTh;
        }
    }
    public static int suitForTargetLevel(int currentLevel,int targetLevel,int exp) throws Exception {
        if (currentLevel < 0 || targetLevel <= currentLevel){
            throw new Exception("current level not valid");
        }
        int result = 0;

        int tenTh = currentLevel / 10 + 1;
        int targetTenth = targetLevel / 10 + 1;
        int targetExtra = targetLevel % 10;
        int extra = currentLevel % 10;
        int suitForOneLevel = tenTh - exp;
        int suitForCurrentTenth = tenTh * (10 + 1 - extra * tenTh);
        for (int i = tenTh + 1;i < targetTenth;i++){
            result += suitForTenth(i);
        }
        result += suitForCurrentTenth;
        result += suitForOneLevel;
        result += targetTenth * targetExtra;
        return result;
    }
    private static int suitForTenth(int tenth){
        return 10 * tenth;
    }
    public static <T> T getRestResult(String url, Class<T> tClass) throws Exception {
        Exception ee = null;
        for (int i = 0; i < 3; i++) {
            try {
                T t = restTemplate.getForObject(url, tClass);
                return t;
            } catch (Exception e) {
                ee = e;
            }
        }
        throw ee;
    }
}
