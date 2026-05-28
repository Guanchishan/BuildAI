package de.kel0002.buildai.util;

import de.kel0002.buildai.BuildAI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;


import java.util.*;

public class ConfigManager {

    public static Dictionary<String, Object> getPayload(String modelName) {

        FileConfiguration config = BuildAI.getInstance().getConfig();
        String payloadpath = "models." + modelName + ".payload";


        if (config.contains(payloadpath)) {
            Dictionary<String, Object> payload = new Hashtable<>();


            List<?> configpayload = config.getList(payloadpath);

            if (configpayload == null) return null;
            for (Object singledic : configpayload){

                if (singledic instanceof LinkedHashMap){
                    LinkedHashMap<?, ?> map = (LinkedHashMap<?, ?>) singledic;
                    Map.Entry<?, ?> entry = map.entrySet().iterator().next();
                    payload.put(entry.getKey().toString(), entry.getValue());
                } else {
                    Bukkit.getLogger().warning("config garbage: the payload has to be structured correctly");
                }
            }

            return payload;
        }
        return null;
    }

    public static String getUrl(String modelName) {
        FileConfiguration config = BuildAI.getInstance().getConfig();
        if (config.contains("models." + modelName + ".endpoint")) {
            return config.getString("models." + modelName + ".endpoint");
        }
        return null;
    }

    public static String getApiKey(String modelName) {
        FileConfiguration config = BuildAI.getInstance().getConfig();
        if (config.contains("models." + modelName + ".api_key")) {
            return config.getString("models." + modelName + ".api_key");
        }
        return null;
    }


    public static ArrayList<String> get_model_list(){
        FileConfiguration config = BuildAI.getInstance().getConfig();
        ArrayList<String> modelNames = new ArrayList<>();

        if (config.getList("model_list") == null) return modelNames;

        for (Object s : config.getList("model_list")){
            modelNames.add(String.valueOf(s));
        }

        return modelNames;
    }



    public static Dictionary<String, Object> replaceInDictionary(Dictionary<String, Object> dictionary, String target, String replacement) {
        Enumeration<String> keys = dictionary.keys();

        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Object value = dictionary.get(key);

            dictionary.put(key, replaceInObject(value, target, replacement));
        }
        return dictionary;
    }

    public static String replaceInString(String value, String target, String replacement) {
        if (value == null) return null;
        return value.replace(target, replacement);
    }

    private static Object replaceInObject(Object value, String target, String replacement) {
        if (value instanceof String) {
            return replaceInString((String) value, target, replacement);
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            for (int i = 0; i < list.size(); i++) {
                ((List<Object>) list).set(i, replaceInObject(list.get(i), target, replacement));
            }
            return list;
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            for (Object key : new ArrayList<>(map.keySet())) {
                ((Map<Object, Object>) map).put(key, replaceInObject(map.get(key), target, replacement));
            }
            return map;
        }
        if (value instanceof Dictionary) {
            Dictionary<Object, Object> dictionary = (Dictionary<Object, Object>) value;
            Enumeration<Object> keys = dictionary.keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                dictionary.put(key, replaceInObject(dictionary.get(key), target, replacement));
            }
            return dictionary;
        }
        return value;
    }


    public static List<String> getVarsinDictionary(Dictionary<String, Object> dictionary) {
         List<String> result = new ArrayList<>();

        Enumeration<String> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Object value = dictionary.get(key);

            addVarsFromObject(value, result);
        }

        return result.isEmpty() ? null : result;
    }

    public static boolean isUnsetVariable(String value) {
        return value != null && value.startsWith("%") && value.endsWith("%");
    }

    private static void addVarsFromObject(Object value, List<String> result) {
        if (value instanceof String) {
            String stringValue = (String) value;
            if (isUnsetVariable(stringValue)) {
                result.add(stringValue);
            }
            return;
        }
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                addVarsFromObject(item, result);
            }
            return;
        }
        if (value instanceof Map) {
            for (Object item : ((Map<?, ?>) value).values()) {
                addVarsFromObject(item, result);
            }
            return;
        }
        if (value instanceof Dictionary) {
            Dictionary<?, ?> dictionary = (Dictionary<?, ?>) value;
            Enumeration<?> keys = dictionary.keys();
            while (keys.hasMoreElements()) {
                addVarsFromObject(dictionary.get(keys.nextElement()), result);
            }
        }
    }
}
