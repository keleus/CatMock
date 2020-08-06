package cn.myzju.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptException;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class CatMock {
    private final static String FILE_NAME = "mock.js";
    private final Context context;
    private final ObjectMapper mapper;

    public CatMock() throws IOException {
        this.mapper = new ObjectMapper();
        Context context = Context.create();
        URL url = CatMock.class.getClassLoader().getResource(FILE_NAME);
        if (url == null) {
            throw new FileNotFoundException();
        }
        String protocol = url.getProtocol();
        //打包为jar环境
        if ("jar".equals(protocol)) {
            JarFile file = new JarFile(new File(CatMock.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
            context.eval(Source.newBuilder("js", new InputStreamReader(file.getInputStream(file.getJarEntry(FILE_NAME))), "mock.js").build());
        } else {
            context.eval(Source.newBuilder("js", url).build());
        }
        this.context = context;
    }

    public CatMock(@NotNull ObjectMapper mapper) throws IOException {
        this.mapper = mapper;
        Context context = Context.create();
        URL url = CatMock.class.getClassLoader().getResource(FILE_NAME);
        if (url == null) {
            throw new FileNotFoundException();
        }
        String protocol = url.getProtocol();
        //打包为jar环境
        if ("jar".equals(protocol)) {
            JarFile file = new JarFile(new File(CatMock.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
            context.eval(Source.newBuilder("js", new InputStreamReader(file.getInputStream(file.getJarEntry(FILE_NAME))), "mock.js").build());
        } else {
            context.eval(Source.newBuilder("js", url).build());
        }
        this.context = context;
    }

    public CatMock(@NotNull File file) throws IOException {
        Context context = Context.create();
        context.eval(Source.newBuilder("js", file).build());
        this.context = context;
        this.mapper = new ObjectMapper();
    }

    public CatMock(@NotNull File file, @NotNull ObjectMapper mapper) throws IOException {
        Context context = Context.create();
        context.eval(Source.newBuilder("js", file).build());
        this.context = context;
        this.mapper = mapper;
    }

    public CatMock(@NotNull URL url) throws IOException {
        Context context = Context.create();
        context.eval(Source.newBuilder("js", url).build());
        this.context = context;
        this.mapper = new ObjectMapper();
    }

    public CatMock(@NotNull URL url, @NotNull ObjectMapper mapper) throws IOException {
        Context context = Context.create();
        context.eval(Source.newBuilder("js", url).build());
        this.context = context;
        this.mapper = mapper;
    }

    public String mock(@NotNull String json) {
        if (json.startsWith("@")) {
            return this.context.getBindings("js").getMember("Mock").getMember("mock").execute(json).asString();
        } else {
            return this.context.eval("js", "JSON.stringify(Mock.mock(" + json + "))").asString();
        }
    }

    public <T> T mockObject(@NotNull String json, Class<T> clazz) throws JsonProcessingException {
        return mapper.readValue(mock(json), clazz);
    }

    public <E> List<E> mockArray(@NotNull String json, Class<E> clazz) throws JsonProcessingException {
        JsonNode jsonNode = mapper.readTree(mock(json));
        List<E> res = new ArrayList<E>();
        if (jsonNode.isArray()) {
            for (JsonNode node : jsonNode) {
                res.add(mapper.readValue(node.toString(), clazz));
            }
        }
        return res;
    }

    public String random(@NotNull String function) throws ScriptException {
        if (function.endsWith(")")) {
            return this.context.eval("js", "Mock.Random." + function).asString();
        } else {
            return this.context.eval("js", "Mock.Random." + function + "()").asString();
        }
    }

    public void extend(String script) {
        this.context.eval("js", "Mock.Random.extend(" + script + ")");
    }

}
