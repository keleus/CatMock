package cn.myzju.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CatContainer implements Cloneable, Serializable {
    private final ObjectMapper mapper;
    private final Map<String, String> params;

    private CatContainer(Map<String, String> params, ObjectMapper mapper) {
        if (params == null) {
            throw new NullPointerException();
        }
        this.mapper = mapper;
        this.params = params;
    }

    public static CatContainer commonContainer() {
        return new CatContainer(new HashMap<String, String>(), new ObjectMapper());
    }

    public static CatContainer concurrentContainer() {
        return new CatContainer(new ConcurrentHashMap<String, String>(), new ObjectMapper());
    }

    public static CatContainer commonContainer(ObjectMapper mapper) {
        if (mapper == null){
            mapper = new ObjectMapper();
        }
        return new CatContainer(new HashMap<String, String>(), mapper);
    }

    public static CatContainer concurrentContainer(ObjectMapper mapper) {
        if (mapper == null){
            mapper = new ObjectMapper();
        }
        return new CatContainer(new ConcurrentHashMap<String, String>(), mapper);
    }

    public String translate(String original) {
        if (original == null || original.isEmpty()) {
            return "";
        }
        String res = original;
        int index = -1;
        while (res.lastIndexOf("${") >= 0) {
            int start = res.lastIndexOf("${");
            if (start == index) {
                break;
            }
            index = start;
            int end = res.indexOf("}", start);
            if (end > start) {
                String bind = res.substring(start, end + 1);
                String key = bind.substring(2, bind.length() - 1);
                if (params.containsKey(key)) {
                    res = res.replace(bind, params.get(key));
                }
            }
        }
        return res;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public boolean containsKey(String key) {
        return params.containsValue(key);
    }

    public boolean containsValue(String value) {
        return params.containsValue(value);
    }

    public String get(String key) {
        return params.get(key);
    }

    public void put(String key, String value) {
        if (value != null) {
            try {
                JsonNode jsonNode = mapper.readTree(value);
                if (jsonNode.isObject()) {
                    readJSONObject(key, jsonNode);
                } else if (jsonNode.isArray()) {
                    readJSONArray(key, jsonNode);
                } else {
                    params.put(key, value);
                }
            } catch (JsonProcessingException e) {
                params.put(key, value);
            }
        }
    }

    public void remove(String key) {
        if (params.containsKey(key)) {
            for (String k : params.keySet()) {
                if (k.startsWith(key)) {
                    params.remove(k);
                }
            }
        }
    }

    public void clear() {
        params.clear();
    }

    private void readJSONObject(String code, JsonNode jsonNode) {
        params.put(code, jsonNode.toString());
        Iterator<String> iterator = jsonNode.fieldNames();
        while (iterator.hasNext()) {
            String key = iterator.next();
            JsonNode childNode = jsonNode.get(key);
            if (childNode.isObject()) {
                readJSONObject(code + "." + key, childNode);
            } else if (childNode.isArray()) {
                readJSONArray(code + "." + key, childNode);
            } else if (childNode.isValueNode()) {
                params.put(code + "." + key, childNode.textValue());
            }
        }
    }

    private void readJSONArray(String code, JsonNode jsonNode) {
        int index = 0;
        params.put(code, jsonNode.toString());
        for (JsonNode childNode : jsonNode) {
            if (childNode.isObject()) {
                readJSONObject(code + "[" + index + "]", childNode);
            } else if (childNode.isArray()) {
                readJSONArray(code + "[" + index + "]", childNode);
            } else if (childNode.isValueNode()) {
                params.put(code + "[" + index + "]", childNode.textValue());
            }
            index++;
        }
    }

    public ObjectMapper getMapper() {
        return this.mapper;
    }
}
