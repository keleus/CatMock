# CatMock

CatMock是一个[mock.js](http://mockjs.com/)的Java封装库。使用JDK自带的js脚本引擎直接调用mock.js脚本，实现对mock.js的统一。让接口设计阶段产生的项目资产能被后端测试复用。

## Maven
中央仓库地址：[https://search.maven.org/artifact/cn.myzju.mock/CatMock](https://search.maven.org/artifact/cn.myzju.mock/CatMock)

```xml
<dependency>
    <groupId>cn.myzju.mock</groupId>
    <artifactId>CatMock</artifactId>
    <version>1.2.0</version>
</dependency>
```

## Mock使用说明

### 获取CatMock对象

```java
//使用内置的mock.js文件初始化
CatMock catMock = new CatMock();
//使用外置的mock.js文件进行初始化
CatMock catMock = new CatMock(new FileReader("{path}/mock.js"));
```

### Random

传入参数：`function`或`function('args')`，返回类型为String

```java
catMock.random("string"); //👉P^7
catMock.random("lower(\"HELLO\")");//👉hello
```

**当前已知不兼容函数：** 
- dataImage()

### Mock

示例可参考[mock.js官方示例](http://mockjs.com/examples.html)

**生成Java对象**

传入字符串必须以`{`开始，并以`}`结束

```java
Person person = catMock.mockObject("{\"name\":\"@string\"}",Person.class);
```

**生成Java List**

传入字符串必须以`[`开始，并以`]`结束，如果传入字符串非JsonArray格式，会返回一个size为0的List；如果传入字符串非json格式，可能会报错。

```java
List<Person> persons = catMock.mockArray("[{\"name\":\"@string\"},{\"name\":\"@string\"}]",Person.class); 
```

**生成String**

mock方法传入非JSON格式的字符串（如下`e.g.1`所示）时，前后一定要加上单引号。

```java
//e.g.1
catMock.mock("'@name'");//👉Anna Jackson
//e.g.2
catMock.mock("{\n'regexp|1-5': /\\d{5,10}\\-/\n}");//👉{"regexp": "5912165-6588485-0462848-"}
```

### Extend

生成CatMock对象后，可以通过extend加载自定义函数。

例如新增一个名为`constellation`，用于获取随机星座名称的方法，JavaScript代码如下：

```javascript
{
    constellation: function(date) {
        var constellations = ['白羊座', '金牛座', '双子座', '巨蟹座', '狮子座', '处女座', '天秤座', '天蝎座', '射手座', '摩羯座', '水瓶座', '双鱼座']
        return this.pick(constellations)
    }
}
```

在CatMock中，将脚本代码直接以String类型传入extend函数：

```java
CatMock catMock = new CatMock()
catMock.extend("{\n" +
        "    constellation: function(date) {\n" +
        "        var constellations = ['白羊座', '金牛座', '双子座', '巨蟹座', '狮子座', '处女座', '天秤座', '天蝎座', '射手座', '摩羯座', '水瓶座', '双鱼座']\n" +
        "        return this.pick(constellations)\n" + 
        "    }\n" +
        "}");
catMock.random("constellation")//👉水瓶座
catMock.mock("'@constellation'")//👉白羊座
```
## Container使用说明

### 获取CatContainer

```java
//获取的容器内部采用HashMap存储变量
CatContainer container = CatContainer.commonContainer();
//获取的容器内部采用ConcurrentHashMap存储变量
CatContainer container = CatContainer.concurrentContainer();
```

### put()

和常规Map的put有所不同，CatContainer会深入解析符合json串格式的值。

运行如下代码，会在CatContainer内置的Map中产生`a1`、`a1[0]`、`a1[1]`、`a1[0].data`、`a1[0].data.token`、`a1[1].data`、`a1[1].data.token`的`<K, V>`键值对。

```java
container.put("a1","[{\"data\":{\"token\":\"AiOiJKV1\"}},{\"data\":{\"token\":\"J9eXCt9c\"}}]");
```

### get()

通过key值获取内置Map中存储的对应value

```java
container.get("a1[0].data.token")//👉AiOiJKV1
```

### translate()

将传入字符串中的`${key}`替换成相应的值，支持嵌套`${${}}`从内至外顺序解析，如下所示。

```java
//加载数据
container.put("a1","[{\"data\":{\"token\":\"AiOiJKV1\"}},{\"data\":{\"token\":\"J9eXCt9c\"}},{\"data\":{\"tokens\":[\"J9eXCt9c\",\"AiOiJKV1\"]}}]");
container.put("a2","data.token");

container.translate("Bearer ${a1[2].${a2}s[0]}")//👉Bearer J9eXCt9c
```

### getParams()

获得用于存储`<K, V>`键值对的内置Map对象，可以通过`container.getParams().put()`插入不愿深入解析的json字符串。

**其余函数为内置Map的封装，用法与Map相同**

## Development Plan

- mock.js后续版本的兼容
- 修复JSON处理引擎为Jackson后可能存在的BUG
- Java正则工具

## License

CatMock is available under the terms of the [MIT License](http://opensource.org/licenses/MIT).
