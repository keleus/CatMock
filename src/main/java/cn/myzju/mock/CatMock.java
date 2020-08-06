package cn.myzju.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class CatMock {
    private final ScriptEngine engine;
    private final static String FILE_NAME = "mock.js";
    private final ObjectMapper mapper;

    public CatMock() throws IOException, ScriptException {
        this.engine = new ScriptEngineManager().getEngineByName("js");
        this.mapper = new ObjectMapper();
        URL url = CatMock.class.getClassLoader().getResource(FILE_NAME);
        if (url == null) {
            throw new FileNotFoundException();
        } else {
            String protocol = url.getProtocol();
            //打包为jar环境
            if ("jar".equals(protocol)) {
                JarFile file = new JarFile(new File(CatMock.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
                this.engine.eval(new InputStreamReader(file.getInputStream(file.getJarEntry(FILE_NAME))));
            } else if ("file".equals(protocol)) {
                FileReader reader = new FileReader(url.getFile());
                this.engine.eval(reader);
            }
        }
    }

    public CatMock(Reader reader) throws ScriptException {
        this.engine = new ScriptEngineManager().getEngineByName("js");
        this.mapper = new ObjectMapper();
        this.engine.eval(reader);
    }

    public CatMock(ObjectMapper mapper) throws IOException, ScriptException {
        this.engine = new ScriptEngineManager().getEngineByName("js");
        if (mapper == null){
            mapper = new ObjectMapper();
        }
        this.mapper = mapper;
        URL url = CatMock.class.getClassLoader().getResource(FILE_NAME);
        if (url == null) {
            throw new FileNotFoundException();
        } else {
            String protocol = url.getProtocol();
            //打包为jar环境
            if ("jar".equals(protocol)) {
                JarFile file = new JarFile(new File(CatMock.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
                this.engine.eval(new InputStreamReader(file.getInputStream(file.getJarEntry(FILE_NAME))));
            } else if ("file".equals(protocol)) {
                FileReader reader = new FileReader(url.getFile());
                this.engine.eval(reader);
            }
        }
    }

    public CatMock(Reader reader,ObjectMapper mapper) throws ScriptException {
        this.engine = new ScriptEngineManager().getEngineByName("js");
        if (mapper == null){
            mapper = new ObjectMapper();
        }
        this.mapper = mapper;
        this.engine.eval(reader);
    }

    public void extend(String script) throws ScriptException {
        this.engine.eval("Mock.Random.extend(" + script + ")");
    }

    public String mock(String json) throws ScriptException {
        return this.engine.eval("JSON.stringify(Mock.mock(" + json + "))").toString();
    }

    public <T> T mockObject(String json, Class<T> clazz) throws JsonProcessingException, ScriptException {
        return mapper.readValue(mock(json), clazz);
    }

    /**
     * List转换方法
     *
     * @param json json字符串
     * @param <T>  泛型，具体类型由调用方法的地方指定
     * @return 返回转换结果
     * @throws ScriptException         mock.js执行错误异常
     * @throws JsonProcessingException json格式错误
     */
    public <T> List<T> mockArray(String json, Class<T> clazz) throws ScriptException, JsonProcessingException {
        JsonNode jsonNode = mapper.readTree(mock(json));
        List<T> res = new ArrayList<T>();
        if (jsonNode.isArray()) {
            for (JsonNode node : jsonNode) {
                res.add(mapper.readValue(node.toString(), clazz));
            }
        }
        return res;
    }

    public String random(String function) throws ScriptException {
        if (function.endsWith(")")) {
            return this.engine.eval("Mock.Random." + function).toString();
        } else {
            return this.engine.eval("Mock.Random." + function + "()").toString();
        }
    }

    public ObjectMapper getMapper() {
        return this.mapper;
    }
}
