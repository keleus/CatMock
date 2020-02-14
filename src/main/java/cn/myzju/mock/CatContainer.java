package cn.myzju.mock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CatContainer implements Cloneable, Serializable {
    private final Map<String, String> params;

    private CatContainer(Map<String, String> params) {
        if (params == null) {
            throw new NullPointerException();
        }
        this.params = params;
    }

    public static CatContainer commonContainer() {
        return new CatContainer(new HashMap<String, String>());
    }

    public static CatContainer concurrentContainer() {
        return new CatContainer(new ConcurrentHashMap<String, String>());
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
                if (value.startsWith("{") && value.endsWith("}")) {
                    readJSONObject(key, JSON.parseObject(value));
                } else if (value.startsWith("[") && value.endsWith("]")) {
                    readJSONArray(key, JSON.parseArray(value));
                } else {
                    params.put(key, value);
                }
            } catch (JSONException e){
                //处理非json格式，但以{}或[]开始结束的字符串
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

    private void readJSONObject(String code, JSONObject jsonObject) {
        params.put(code, jsonObject.toJSONString());
        for (JSONObject.Entry<String, Object> entry : jsonObject.entrySet()) {
            if (entry.getValue() instanceof JSONObject) {
                readJSONObject(code + "." + entry.getKey(), jsonObject.getJSONObject(entry.getKey()));
            } else if (entry.getValue() instanceof JSONArray) {
                readJSONArray(code + "." + entry.getKey(), jsonObject.getJSONArray(entry.getKey()));
            } else if (entry.getValue() != null) {
                params.put(code + "." + entry.getKey(), jsonObject.getString(entry.getKey()));
            }
        }
    }

    private void readJSONArray(String code, JSONArray jsonArray) {
        int index = 0;
        params.put(code, jsonArray.toJSONString());
        for (Object object : jsonArray) {
            if (object instanceof JSONObject) {
                readJSONObject(code + "[" + index + "]", jsonArray.getJSONObject(index));
            } else if (object instanceof JSONArray) {
                readJSONArray(code + "[" + index + "]", jsonArray.getJSONArray(index));
            } else if (jsonArray.getString(index) != null) {
                params.put(code + "[" + index + "]", jsonArray.getString(index));
            }
            index++;
        }
    }
}
