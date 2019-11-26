package jn.mjz.aiot.jnuetc.util;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class GsonUtil {

    private static Gson instance;


    public static Gson getInstance() {
        if (instance == null) {
            instance = new Gson();
        }
        return instance;
    }

    public static <T> void parseJsonArrayAdd2List(String result, List<T> objectList, Class<T> className, IGsonListener iGsonListener) {
        int cnt = 0, oldCount = objectList.size();
        //Json的解析类对象
        JsonParser parser = new JsonParser();
        //将JSON的String 转成一个JsonArray对象
        JsonArray jsonArray = parser.parse(result).getAsJsonArray();
        //加强for循环遍历JsonArray
        for (JsonElement c : jsonArray) {
            //使用GSON，直接转成Bean对象
            objectList.add(getInstance().fromJson(c, className));
            cnt++;
        }
        iGsonListener.OnItemAdded(oldCount, cnt);
    }

    public static <T> List<T> parseJsonArray2ObejctList(String result, Class<T> className) {
        List<T> list = new ArrayList<>();
        //Json的解析类对象
        JsonParser parser = new JsonParser();
        //将JSON的String 转成一个JsonArray对象
        JsonArray jsonArray = parser.parse(result).getAsJsonArray();
        //加强for循环遍历JsonArray
        for (JsonElement c : jsonArray) {
            //使用GSON，直接转成Bean对象
            list.add(getInstance().fromJson(c, className));
        }
        return list;
    }

    public interface IGsonListener {
        void OnItemAdded(int oldCount, int addedNumber);
    }


}
