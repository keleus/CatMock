package cn.myzju.mock;

import com.alibaba.fastjson.JSONObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.jar.JarFile;

public class CatMock {
    private final ScriptEngine engine;
    private final static String FILE_NAME = "mock.js";

    public CatMock() throws IOException, ScriptException {
        this.engine = new ScriptEngineManager().getEngineByName("js");
        URL url = CatMock.class.getClassLoader().getResource(FILE_NAME);
        if (url == null) {
            throw new FileNotFoundException();
        } else {
            String protocol = url.getProtocol();
            //打包为jar环境
            if("jar".equals(protocol)){
                JarFile file = new JarFile(new File(CatMock.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
                this.engine.eval(new InputStreamReader(file.getInputStream(file.getJarEntry(FILE_NAME))));
            }else if("file".equals(protocol)){
                FileReader reader = new FileReader(url.getFile());
                this.engine.eval(reader);
            }
        }
    }

    public CatMock(Reader reader) throws ScriptException {
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
