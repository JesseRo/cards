package cards.service.impl;

import cards.entity.*;
import cards.entity.json.CardPickedResult;
import cards.entity.json.Result;
import cards.entity.rest.*;
import cards.entity.rest.profile.Profile;
import cards.repository.*;
import cards.service.BusinessService;
import cards.service.Robot;
import cards.util.Utils;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by asus on 2017/1/8.
 */
@Service
public class BusinessServiceImpl implements BusinessService {
    @Autowired
    private HttpSession session;
    @Autowired
    private Robot robot;
    @Autowired
    private TypeRepository typeRepository;
    @Autowired
    private AppRepository appRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CardRepository cardRepository;
    private Gson gson = new Gson();
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private UserRepository userRepository;
    @Value(value = "${charge.url}")
    private String chargeUrl;
    @Transactional
    public Map<App,List<Type>> cheapestItems(String openId,List<GameLevel> gls) throws Exception {
        String url = "https://api.steampowered.com/IPlayerService/GetBadges/v1/?key=DF768D875E848CE96C05567249F56EEC&steamid=" + openId;
        UserLevelInfo userInfo = Utils.getRestResult(url, UserLevelInfo.class);
        List<GameLevel> gameLevels = userInfo.getResponse().getBadges();
        if (gameLevels == null){
            gameLevels = new ArrayList<>();
        }
        gls.addAll(gameLevels);
        List<String> level5 = gameLevels.stream().filter(p -> p.getLevel() == 5).map(p->String.valueOf(p.getAppid())).collect(Collectors.toList());
        List<App> apps = appRepository.findAll(level5);
        List<Type> types;
        if (apps == null || apps.isEmpty()){
            types = typeRepository.findCheapest();
        }else {
            types = typeRepository.findCheapest(apps);
        }
        Map<App,List<Type>> typeGroup = types.stream().collect(Collectors.groupingBy(Type::getApp));
        return typeGroup;
    }
    @Override
    @Transactional
    public CardPickedResult upLevel(String openId, Integer targetLevel) throws Exception {
        clearUnsubmit(session);
        User user = userRepository.findOne(openId);
        Float money = 99999f;
        Integer level = user.getLevel();
        List<GameLevel> gameLevels = new ArrayList<>();
        Map<App,List<Type>> typeGroup = cheapestItems(openId,gameLevels);
        List<App> apps = typeGroup.keySet().stream().sorted((one, another) -> one.getPrice() > another.getPrice() ? 1 : -1).collect(Collectors.toList());
        Integer totalSuitNumber = 0;
        Integer targetSuitNumber;
        if (targetLevel == -1){
            targetSuitNumber = -1;
        }else {
            targetSuitNumber = Utils.suitForTargetLevel(level,targetLevel,user.getExp());
        }
        List<App> cards = new ArrayList<>();
        List<Integer> cardNumbers = new ArrayList<>();
        List<CardSet> tradeItems = new ArrayList<>();
        for(App app : apps){
            List<Type> types = typeGroup.get(app);
            Set<String> classIds = types.stream().map(Type::getClassid).collect(Collectors.toSet());
            Integer suitsNumber = app.getNumber();
            if (suitsNumber == null || suitsNumber <= 0){
                continue;
            }
//            List<Long> numbers = itemRepository.countByClassidIn(classIds);
//            if (numbers.size() < classIds.size()){
//                continue;
//            }
//            Optional<Long> optionalNumber = numbers.stream().min(Long::compare);
//            Integer suitsNumber;
//            if (optionalNumber.isPresent()){
//                suitsNumber = optionalNumber.get().intValue();
//            }else {
//                continue;
//            }
            List<Integer> gameLevel = gameLevels.stream().filter(p -> String.valueOf(p.getAppid()).equals(app.getAppId())).map(GameLevel::getLevel).collect(Collectors.toList());
            Integer gl = 0;
            if (gameLevel.size() > 0){
                assert gameLevel.size() == 1;
                gl = gameLevel.get(0);
            }
            Integer affordedNumber = affordAndAvailableLessThanTarget(suitsNumber, gl, money, targetSuitNumber, app.getPrice());
            if (affordedNumber == -1){
                break;
            }
            if (targetSuitNumber != -1){
                targetSuitNumber -= affordedNumber;
            }
            List<CardSet> cardSets = new ArrayList<>();
            for (int j = 0;j < affordedNumber;j ++){
                CardSet cardSet = new CardSet();
                cardSet.setApp(app);
                cardSets.add(cardSet);
            }
            tradeItems.addAll(cardSets);

            List<Item> cardAllItems = new ArrayList<>();
            for(int i = 0;i < types.size();i++){
                List<Item> it = findItemLimit(types.get(i).getClassid(), affordedNumber);
                it.forEach(p -> p.setPlaced(true));
                cardAllItems.addAll(it);
                if (affordedNumber != it.size() || affordedNumber != cardSets.size()){
                    it.hashCode();
                }
                for (int k = 0;k < affordedNumber; k++){
                    cardSets.get(k).add(it.get(k));
                }
            }
            itemRepository.save(cardAllItems);
            money -= affordedNumber * app.getPrice();
            cards.add(app);
            cardNumbers.add(affordedNumber);
            totalSuitNumber += affordedNumber;
        }
        Integer afterLevel = Utils.levelUp(user.getLevel(), totalSuitNumber + user.getExp() / 100);
        if (targetSuitNumber != -1){
            assert afterLevel == targetLevel;
        }
        CardPickedResult result = new CardPickedResult(afterLevel,cards,cardNumbers,user.getMoney() - money,user.getMoney(),tradeItems);

        session.setAttribute("unsubmittedOrder",result);
        return result;
    }

    @Override
    @Transactional
    public Integer maxLevel(String openId) throws Exception{
        User user = userRepository.findOne(openId);
        Integer level = user.getLevel();
        List<GameLevel> gameLevels = new ArrayList<>();
        Map<App,List<Type>> typeGroup = cheapestItems(openId,gameLevels);
        List<App> apps = new ArrayList<>(typeGroup.keySet());
        Integer availableSets  = 0;
        for (App app :apps){
            Optional<GameLevel> gameLevel = gameLevels.stream()
                    .filter(p->p.getAppid().toString().equals(app.getAppId()))
                    .findFirst();
            Integer available;
            if (gameLevel.isPresent()){
                available = 5 - gameLevel.get().getLevel();
            }else {
                available = 5;
            }
            availableSets += Integer.min(available, app.getNumber());
        }
        return Utils.levelUp(level, availableSets + user.getExp() / 100);
    }

    private void clearUnsubmit(HttpSession session) {
        if (session.getAttribute("unsubmittedOrder") != null) {
            CardPickedResult result = (CardPickedResult) session.getAttribute("unsubmittedOrder");
            result.allItems().forEach(p->p.setPlaced(false));
            itemRepository.save(result.allItems());
        }
    }

    @Override
    @Transactional
    public boolean buy(String openId){
        User user = userRepository.findOne(openId);
        if (session.getAttribute("unsubmittedOrder") != null){
            CardPickedResult result = (CardPickedResult)session.getAttribute("unsubmittedOrder");
            if (result.getBillMoney() <= user.getMoney()) {
                String bagJson = user.getBag();
                Bag bag = null;
                if (bagJson != null && !bagJson.trim().isEmpty()) {
                    bag = gson.fromJson(bagJson, Bag.class);
                }
                Bag newBag = Bag.merge(bag, new Bag(result.getItems()));
                user.setBag(gson.toJson(newBag.init()));
                user.setMoney(user.getMoney() - result.getBillMoney());
                userRepository.save(user);
                session.removeAttribute("unsubmittedOrder");
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }
    @Override
    @Transactional
    public Result trade(String openId){
        User user = userRepository.findOne(openId);
        String bagJson = user.getBag();
        if (bagJson == null || !bagJson.isEmpty()) {
            waitAccept(user);
            return Result.Success("");
        }else {
            return Result.Failure("背包是空的", Result.EMPTY_BAG_EXCEPTION);
        }
    }

//    @Transactional
//    @Override
//    public void waitFriend(User user) {
//        String partner = String.valueOf(Long.valueOf(user.getId()) + 76561197960265728L);
//        long currentTime = System.currentTimeMillis();
//        Thread thread = new Thread(()->{
//            try {
//                addFriend(partner);
//                String url = "http://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key=F9E07DB151F050DB43E0FFAAA9C0FBB7&steamid=76561198177687081&relationship=friend";
//                do{
//                    Friends friends = Utils.getRestResult(url, Friends.class);
//                    List<Friend> friendList = friends.getFriendslist().getFriends();
//                    if (friendList.stream().filter(p -> p.getRelationship().equals("friend")).map(Friend::getSteamid).collect(Collectors.toSet()).contains(partner)) {
//                        System.out.println("friend " + partner + " added!");
//                        waitAccept(user);
//                        return;
//                    }
//                    try {
//                        Thread.sleep(10 * 1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                } while (System.currentTimeMillis() - currentTime < 10 * 60 * 1000);
//                remove(partner);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//        thread.start();
//    }

    @Transactional
    private void waitAccept(User user) {
        String partner = user.getId();
        long currentTime = System.currentTimeMillis();
        final String param = partner + "|" + user.getToken() + "|";
        System.out.println("param:" + param);
        String bagJson = user.getBag();
        if (bagJson == null || !bagJson.isEmpty()) {
            Bag bag = gson.fromJson(bagJson, Bag.class);
            List<CardSet> cardSets = bag.getItems();
            List<List<CardSet>> cardSeqs = new ArrayList<>();
            Thread thread = new Thread(()->{
                try {
                    List<String> tradeIds = sendOrders(param, cardSets, cardSeqs);
                    tradeIds = tradeIds.stream().filter(Objects::nonNull).collect(Collectors.toList());
                    do {
                        Thread.sleep(10 * 1000);
                        if (tradeIds.size() == 0) {
                            break;
                        }
                        for (int i = 0; i < tradeIds.size(); i++) {
                            String tradeId = tradeIds.get(i);
                            List<CardSet> cardSeq = cardSeqs.get(i);
                            String url = "http://api.steampowered.com/IEconService/GetTradeOffer/v1/?key=DF768D875E848CE96C05567249F56EEC&tradeofferid=" + tradeId + "&language=en_us";
                            TradeOfferState tradeOfferState = Utils.getRestResult(url, TradeOfferState.class);
                            switch (tradeOfferState.getResponse().getOffer().getTrade_offer_state()) {
                                case 3:
                                    bag.remove(cardSeq);
                                    List<Item> items = new ArrayList<>();
                                    for (CardSet cardSet : cardSeq){
                                        items.addAll(cardSet.getItems());
                                    }
                                    itemRepository.delete(items);
                                    historyRepository.save(new History(user.getId(), gson.toJson(cardSeq), tradeId));
                                    tradeIds.remove(tradeId);
                                case 6:
                                case 7:
                                    cancelOrder(tradeId + "|" + partner);
                                    Thread.sleep(5 * 1000);
                                    tradeIds.remove(tradeId);
                            }
                        }

                    }while (System.currentTimeMillis() - currentTime < 10 * 60 * 1000);
                    user.setBag(gson.toJson(bag.init()));
                    userRepository.save(user);
                    for (String tradeId : tradeIds){
                        cancelOrder(tradeId + "|" + partner);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
    }

    private Integer affordAndAvailableLessThanTarget(Integer suitsNumber,Integer gameLevel,Float money,Integer targetSuitNumber, Float price) {
        Integer it = affordAndAvailable(suitsNumber,gameLevel,money,price);
        if (targetSuitNumber != -1){
            if (targetSuitNumber == 0){
                return -1;
            }else {
                return Integer.min(it, targetSuitNumber);
            }
        }else {
            return it;
        }
    }

    private Integer affordAndAvailable(Integer suitsNumber,Integer gameLevel,Float money, Float price) {
        Float a = money / price;
        Integer afford = a.intValue();
        if (afford == 0){
            return -1;
        }
        return Integer.min(Integer.min(afford, 5 - gameLevel), suitsNumber);
    }

    @Transactional
    private List<Item> findItemLimit(String classid, Integer number){
        Specifications<Item> specifications = Specifications.where(null);
        specifications = specifications.and(SpecificationFactory.getEqual("placed", false));
        specifications = specifications.and(SpecificationFactory.getEqual("classid", classid));
        Pageable pageable = new PageRequest(0,number);
        return itemRepository.findAll(specifications, pageable).getContent();
    }

    @Override
    @Transactional
    public User info(String openId) throws Exception {
        String url = "https://api.steampowered.com/IPlayerService/GetBadges/v1/?key=DF768D875E848CE96C05567249F56EEC&steamid=" + openId;
        UserLevelInfo userInfo = Utils.getRestResult(url, UserLevelInfo.class);
        String profileUrl = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=DF768D875E848CE96C05567249F56EEC&steamids="+openId;
        Profile profile = Utils.getRestResult(profileUrl, Profile.class);
        User user = userRepository.findOne(openId);
        if (user == null){
            user = new User();
            user.setId(openId);
            user.setMoney(0f);
        }
        user.setName(profile.getResponse().getPlayers().get(0).getPersonaname());
        Integer exp = userInfo.getResponse().getPlayer_xp_needed_current_level();
        Integer level = userInfo.getResponse().getPlayer_level();
        user.setExp(exp == null ? 0 : exp);
        user.setLevel(level == null ? 0 : level);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void saveTradUrl(String openId, String url) {
        String token = UriComponentsBuilder.fromUriString(url).build().getQueryParams().get("token").get(0);
        if (token != null && !token.isEmpty()) {
            User user = userRepository.findOne(openId);
            user.setTradeUrl(url);
            user.setToken(token);
            userRepository.save(user);
        }
    }

    @Transactional
    @Override
    public ChargeResult charge(String openId, String oid) throws Exception {
        String url = chargeUrl + "?oid=" + oid;
        ChargeResult chargeResult = Utils.getRestResult(url,ChargeResult.class);
        if (chargeResult.isResult()){
            User user = userRepository.findOne(openId);
            user.setMoney(user.getMoney() + chargeResult.getPayment());
            userRepository.save(user);
        }
        return chargeResult;
    }
    private List<String> sendOrders(String id,List<CardSet> items,List<List<CardSet>> cardSeqs) {
        List<String>  results = new ArrayList<>();
        boolean loop = true;
        while (loop){
            List<CardSet > seg;
            if (items.size() > 10){
                seg = items.subList(0, 10);
                items.removeAll(seg);
            }else {
                seg = items;
                loop = false;
            }
            cardSeqs.add(seg);
            StringBuilder fullCommand = new StringBuilder(id);
            for (CardSet cardSet :seg){
                for (Item item : cardSet.getItems())
                fullCommand.append(item.getId()).append(",");
            }
            try {
                results.add(sendOrder_(fullCommand.toString()));
            } catch (RemoteException | ServiceException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    private String sendOrder_(String param) throws RemoteException, ServiceException {
        int steamErrorCount = 0;
        String tradeId = sendOrder(param);
        while (tradeId.equals("false1")) {
            try {
                Thread.sleep(20 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tradeId = sendOrder(param);
            steamErrorCount++;
            if (steamErrorCount >= 5) {
                return null;
            }
        }
        if (!tradeId.equals("false2")) {
            tradeId = tradeId.split(":")[1];
        } else {
            return null;
        }
        return tradeId;
    }

    private String sendOrder(String param) throws RemoteException, ServiceException {
        for(int i = 0;i < 5;i++){
            try {
                return robot.send("send", param);
            }catch (RemoteException | ServiceException e){
                if (i == 4){
                    throw e;
                }
            }
        }
        return null;
    }
    private String cancelOrder(String param) throws RemoteException, ServiceException {
        for(int i = 0;i < 5;i++){
            try {
                return robot.send("cancel", param);
            }catch (RemoteException | ServiceException e){
                if (i == 4){
                    throw e;
                }
            }
        }
        return null;
    }
    private String addFriend(String param) throws RemoteException, ServiceException {
        return robot.send("add", param);
    }
    private String remove(String param) throws RemoteException, ServiceException {
        return robot.send("remove", param);
    }
    private String chat(String param) throws RemoteException, ServiceException {
        return robot.send("chat", param);
    }
}
