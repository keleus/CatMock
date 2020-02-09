package cn.myzju.mock;

import com.alibaba.fastjson.JSONObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.List;

public class CatMock {
    private final ScriptEngine engine;
    private final static String FILE_NAME = "mock.js";

    public CatMock() throws FileNotFoundException, ScriptException {
        URL url = CatMock.class.getClassLoader().getResource(FILE_NAME);
        if (url == null) {
            throw new FileNotFoundException();
        }
        FileReader reader = new FileReader(url.getFile());
        this.engine = new ScriptEngineManager().getEngineByName("js");
        this.engine.eval(reader);
    }

    public CatMock(FileReader reader) throws FileNotFoundException, ScriptException {
        this.engine = new ScriptEngineManager().getEngineByName("js");
        this.engine.eval(reader);
    }

    public void extend(String script) throws ScriptException {
        this.engine.eval("Random.extend(" + script + ")");
    }

    public String mock(String json) throws ScriptException {
        return this.engine.eval("JSON.stringify(Mock.mock(" + json + "))").toString();
    }

    public<T> T mockObject(String json, Class<T> clazz) throws ScriptException {
        return JSONObject.parseObject(mock(json), clazz);
    }

    public<T> List<T> mockArray(String json, Class<T> clazz) throws ScriptException {
        return JSONObject.parseArray(mock(json), clazz);
    }

    public String random(String function) throws ScriptException {
        if (function.endsWith(")")) {
            return this.engine.eval("Mock.Random." + function).toString();
        } else {
            return this.engine.eval("Mock.Random." + function + "()").toString();
        }
    }
}
