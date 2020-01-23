package jn.mjz.aiot.jnuetc.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 19622
 */
public class GsonUtil {

    private static Gson instance;

    public static Gson getInstance() {
        if (instance == null) {
            instance = new Gson();
        }
        return instance;
    }

    /**
     * 把json数据添加到list后面
     *
     * @param result     待转换的数据
     * @param objectList 待盛放数据的list
     * @param className  类型
     * @param iParseList 完成后的回调
     * @param <T>        类型
     */
    public static <T> void parseJsonArrayAdd2List(String result, List<T> objectList, Class<T> className, IParseList iParseList) {
        int cnt = 0, oldCount = objectList.size();
        //将JSON的String 转成一个JsonArray对象
        JsonArray jsonArray = JsonParser.parseString(result).getAsJsonArray();
        //加强for循环遍历JsonArray
        for (JsonElement c : jsonArray) {
            //使用GSON，直接转成Bean对象
            objectList.add(getInstance().fromJson(c, className));
            cnt++;
        }
        iParseList.onParseFinish(oldCount, cnt);
    }

    /**
     * 把json数据解析为list
     *
     * @param result    待解析的数据
     * @param className 类型
     * @param <T>       类型
     * @return List
     */
    public static <T> List<T> parseJsonArray2ObjectList(String result, Class<T> className) {
        List<T> list = new ArrayList<>();
        //将JSON的String 转成一个JsonArray对象
        JsonArray jsonArray = JsonParser.parseString(result).getAsJsonArray();
        //加强for循环遍历JsonArray
        for (JsonElement c : jsonArray) {
            //使用GSON，直接转成Bean对象
            list.add(getInstance().fromJson(c, className));
        }
        return list;
    }

    public interface IParseList {
        /**
         * 解析结束后
         *
         * @param oldCount    原来list的数目
         * @param addedNumber 增加的数目
         */
        void onParseFinish(int oldCount, int addedNumber);
    }


}
