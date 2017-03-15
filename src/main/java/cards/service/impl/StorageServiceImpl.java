package cards.service.impl;

import cards.entity.App;
import cards.entity.Item;
import cards.entity.Storage;
import cards.entity.Type;
import cards.repository.AppRepository;
import cards.repository.ItemRepository;
import cards.repository.TypeRepository;
import cards.service.StorageService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.SystemException;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by asus on 2016/10/23.
 */
@Service("storageService")
public class StorageServiceImpl implements StorageService {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private AppRepository appRepository;
    @Autowired
    private TypeRepository typeRepository;
    @Value("${storage.reload}")
    private String reload;
    @Value("${storage.reload.path}")
    private String jsonFilePath;
    @Value("${storage.statistics}")
    private String statistics;
    private static String storageUrl = "https://steamcommunity.com/profiles/76561198177687081/inventory/json/753/6";
    RestTemplate restTemplate = new RestTemplate();
//
//    private ConcurrentHashMap<String,Item> items = new ConcurrentHashMap<>();
//    private ConcurrentHashMap<String,Type> types = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public List<Item> getAllItems() throws InterruptedException {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().generateNonExecutableJson()
                .create();
        Boolean more = true;
        Integer start = 0;
        while (more){
            try {
                System.out.println("extract json.... from start : " + start);
                byte[] s = restTemplate.getForObject(storageUrl + "?start=" + start, byte[].class);
                JsonReader reader = new JsonReader(new StringReader(new String(s)));
                reader.setLenient(true);

                Storage storage = gson.fromJson(reader, Storage.class);
                if (storage.getMore_start() instanceof Double)
                    start = ((Double) storage.getMore_start()).intValue();
                more = storage.getMore();
                System.out.println(storage.getRgDescriptions().keySet().size());
                System.out.println(storage.getRgInventory().keySet().size());
                addStorage(storage);
            }catch (Exception e){
                e.printStackTrace();
            }
            Thread.sleep(10 * 1000);
        }
        return null;
    }

    @Override
    @Transactional
    public void reload() throws InterruptedException {
//        items.clear();
//        types.clear();
        if (reload != null && reload.equals("web")) {
            typeRepository.deleteAll();
            itemRepository.deleteAll();
            getAllItems();
            statistics();
        }else if (reload != null && reload.equals("file")){
            typeRepository.deleteAll();
            itemRepository.deleteAll();
            getAllItemFromFile();
            statistics();
        }else {
            if (statistics.equals("always")){
                statistics();
            }
        }
    }

//    public void remove(String id){
//        items.remove(id);
//    }

    @Transactional
    private void addStorage(Storage storage){
        itemRepository.save(storage.getRgInventory().values());
        Iterable<Type> types = storage.getRgDescriptions().entrySet().stream()
                .map(p ->
                        {
                            p.getValue().setId(p.getKey());
                            Type type = new Type(p.getValue());
                            type.setApp(appRepository.findOne(p.getValue().getMarket_fee_app()));
                            return type;
                        }
                ).collect(Collectors.toSet());
        typeRepository.save(types);
    }
    @Override
    @Transactional
    public void getAllItemFromFile(){
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().generateNonExecutableJson()
                .create();
        File dir = new File(jsonFilePath);
        if (dir.exists() && dir.isDirectory()){
            for (File file : dir.listFiles()){
                System.out.println("extract json.... from : " + file.getName());
                try {
                    JsonReader reader = new JsonReader(new FileReader(file));
                    reader.setLenient(true);
//                    String line = reader.readLine();
//                    while (line != null){
//                        json += line;
//                        line = reader.readLine();
//                    }
                    Storage storage = gson.fromJson(reader, Storage.class);
                    addStorage(storage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    @Transactional
    public void statistics() {
        List<App> apps = appRepository.findAll();
        List<Type> allTypes = typeRepository.findAll();
        Map<App, List<Type>> typeGroup = allTypes.stream().filter(type -> type.getApp() != null).collect(Collectors.groupingBy(Type::getApp));
        for (App app : apps) {
            List<Type> types = typeGroup.get(app);
            if (types != null) {
                Set<String> classIds = types.stream().map(Type::getClassid).collect(Collectors.toSet());
                List<Long> numbers = itemRepository.countByClassidIn(classIds);
                if (numbers.size() < classIds.size()) {
                    continue;
                }
                Integer suitsNumber;
                Optional<Long> optionalNumber = numbers.stream().min(Long::compare);
                suitsNumber = optionalNumber.map(Long::intValue).orElse(0);
                app.setNumber(suitsNumber);
            }else {
                app.setNumber(0);
            }
        }
        appRepository.save(apps);
        System.out.println("!!!!!");
    }
}
