package cards.web;

import cards.entity.Bag;
import cards.entity.User;
import cards.entity.json.Result;
import cards.entity.rest.ChargeResult;
import cards.repository.UserRepository;
import cards.service.BusinessService;
import com.google.gson.Gson;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.consumer.*;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by asus on 2016/12/25.
 */
@Controller
public class LoginController {
    @Autowired
    private HttpSession session;

    @Value("${card.ip}")
    private String ip;
    @Autowired
    private BusinessService businessService;
    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public @ResponseBody String login(HttpServletResponse response) throws DiscoveryException, MessageException, ConsumerException, IOException {
        ConsumerManager manager;
        String opid = isLogin(session);
        if (opid != null){
            return "ok";
        }
        if (session.getAttribute("consumermanager") == null) {
            manager = new ConsumerManager();
        }else {
            manager = (ConsumerManager)session.getAttribute("consumermanager");
        }
        manager.setAssociations(new InMemoryConsumerAssociationStore());
        manager.setNonceVerifier(new InMemoryNonceVerifier(5000));
        manager.setMinAssocSessEnc(AssociationSessionType.DH_SHA256);
        session.setAttribute("consumermanager", manager);
        String returnToUrl = "http://" + ip + ":8089/return";
        //比较重要，通过关联句柄以及returnURL准备OP需要的参数以及参数值
        List discoveries = manager.discover("https://steamcommunity.com/openid");
        DiscoveryInformation discovered = manager.associate(discoveries);
        session.setAttribute("openid-disc", discovered);
        AuthRequest authReq = manager.authenticate(discovered, returnToUrl);
        if (!discovered.isVersion2()) {
            response.sendRedirect(authReq.getDestinationUrl(true));
        } else {
            response.sendRedirect(authReq.getDestinationUrl(true));
        }
        return "OK";
    }
    @RequestMapping(value = "/return",method = RequestMethod.GET)
    public @ResponseBody String returnURL(HttpServletRequest request,HttpServletResponse response) throws Exception {
        if (session.getAttribute("consumermanager") != null) {
            ConsumerManager manager = (ConsumerManager)session.getAttribute("consumermanager");
            ParameterList params = new ParameterList(request.getParameterMap());
            DiscoveryInformation discovered = (DiscoveryInformation) session.getAttribute("openid-disc");
            StringBuffer url = request.getRequestURL();
            String query = request.getQueryString();
            if (query != null && !query.isEmpty()) {
                url.append("?").append(query);
            }

            VerificationResult verification = manager.verify(url.toString(), params, discovered);
            Identifier verified = verification.getVerifiedId();
            String openId = verified.getIdentifier();
            openId = openId.substring(openId.lastIndexOf('/') + 1);
            session.setAttribute("openId",openId);
            businessService.info(openId);
            response.sendRedirect("profile.html");
        }
        return "OK";
    }
    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    public void logout(HttpServletResponse response) throws IOException {
        session.invalidate();
        response.sendRedirect("main.html");
    }
    private String isLogin(HttpSession session){
        Object openid = session.getAttribute("openId");
        if (openid != null){
            return (String) openid;
        }else {
            return null;
        }
    }
    @RequestMapping(value = "/loginStatus",method = RequestMethod.GET)
    public @ResponseBody
    Result loginStatus(){
        String openid = isLogin(session);
        if (openid != null){
            return Result.Success("");
        }else {
            return Result.Failure("未登录",Result.AUTH_EXCEPTION);
        }
    }
    @RequestMapping(value = "/card",method = RequestMethod.GET)
    public @ResponseBody
    Result cards(){
        String openId = isLogin(session);
        if (openId != null){
            try {
                return Result.Success(userRepository.findOne(openId));
            }catch (Exception e){
                e.printStackTrace();
                return Result.Failure("网路异常",Result.NETWORK_EXCEPTION);
            }
        }else {
            return Result.Failure("未登录",Result.AUTH_EXCEPTION);
        }
    }

    @RequestMapping(value = "/bag/mybag",method = RequestMethod.GET)
    public @ResponseBody
    Result bag(){
        String openId = isLogin(session);
        Gson gson = new Gson();
        if (openId != null){
            try {
                return Result.Success(gson.fromJson(businessService.info(openId).getBag(),Bag.class));
            }catch (Exception e){
                e.printStackTrace();
                return Result.Failure("网路异常",Result.NETWORK_EXCEPTION);
            }
        }else {
            return Result.Failure("未登录",Result.AUTH_EXCEPTION);
        }
    }

    @RequestMapping(value = "/card/maxLevel",method = RequestMethod.GET)
    public @ResponseBody
    Result maxLevel( ){
        String openId = isLogin(session);
        if (openId != null){
            try {
                return Result.Success(businessService.upLevel(openId, -1));
            }catch (Exception e){
                e.printStackTrace();
                return Result.Failure("网路异常",Result.NETWORK_EXCEPTION);
            }
        }else {
            return Result.Failure("未登录",Result.AUTH_EXCEPTION);
        }
    }
    @RequestMapping(value = "/card/upLevel",method = RequestMethod.GET)
    public @ResponseBody
    Result uplevel(Integer targetLevel,Integer maxLevel){
        String openId = isLogin(session);
        if (openId != null){
            try {
                User user = userRepository.findOne(openId);
                if (targetLevel != null) {
                    if (targetLevel > user.getLevel() && targetLevel <= maxLevel) {
                        return Result.Success(businessService.upLevel(openId, targetLevel));
                    }
                }
                return Result.Failure("目标等级必须小于最大等级，且大于当前等级", Result.USER_LEVEL_EXCEPTION);
            }catch (Exception e){
                e.printStackTrace();
                return Result.Failure("网路异常",Result.NETWORK_EXCEPTION);
            }
        }else {
            return Result.Failure("未登录",Result.AUTH_EXCEPTION);
        }
    }
    @RequestMapping(value = "/card/accept",method = RequestMethod.GET)
    public @ResponseBody
    Result accept(){
        String openId = isLogin(session);
        if (openId != null){
            try {
                if (businessService.buy(openId)){
                    return Result.Success("");
                }else {
                    return Result.Failure("余额不足，请充值！",Result.NOT_AFFORDABLE_EXCEPTION);
                }
            }catch (Exception e){
                e.printStackTrace();
                return Result.Failure("网路异常",Result.NETWORK_EXCEPTION);
            }
        }else {
            return Result.Failure("未登录",Result.AUTH_EXCEPTION);
        }
    }
    @RequestMapping(value = "/bag/trade",method = RequestMethod.GET)
    public @ResponseBody
    Result trade(){
        String openId = isLogin(session);
        if (openId != null){
            try {
                return businessService.trade(openId);
            }catch (Exception e){
                e.printStackTrace();
                return Result.Failure("网路异常",Result.NETWORK_EXCEPTION);
            }
        }else {
            return Result.Failure("未登录",Result.AUTH_EXCEPTION);
        }
    }

    @RequestMapping(value = "/profile/myprofile",method = RequestMethod.GET)
    public @ResponseBody
    Result profile(){
        String openId = isLogin(session);
        if (openId != null){
            try {
                return Result.Success(userRepository.findOne(openId));
            }catch (Exception e){
                return Result.Failure("网路异常",Result.NETWORK_EXCEPTION);
            }
        }else {
            return Result.Failure("未登录",Result.AUTH_EXCEPTION);
        }
    }

    @RequestMapping(value = "/profile/save",method = RequestMethod.GET)
    public @ResponseBody
    Result profileSave(String tradeUrl){
        String openId = isLogin(session);
        if (openId != null){
            try {
                businessService.saveTradUrl(openId,tradeUrl.trim());
                return Result.Success("");
            }catch (Exception e){
                return Result.Failure("报价链接错误",Result.TRADE_URL_EXCEPTION);
            }
        }else {
            return Result.Failure("未登录",Result.AUTH_EXCEPTION);
        }
    }

    @RequestMapping(value = "/charge/order",method = RequestMethod.GET)
    public @ResponseBody
    Result chargeByOid(String oid){
        String openId = isLogin(session);
        if (openId != null){
            try {
                ChargeResult chargeResult = businessService.charge(openId, oid.trim());
                if (chargeResult.isResult()){
                    return Result.Success("");
                }
                return Result.Failure("订单号错误或已经充值！",Result.TRADE_URL_EXCEPTION);
            }catch (Exception e){
                e.printStackTrace();
                return Result.Failure("网络异常",Result.NETWORK_EXCEPTION);
            }
        }else {
            return Result.Failure("未登录",Result.AUTH_EXCEPTION);
        }
    }
}
