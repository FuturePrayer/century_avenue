# **Century_Avenue**

*Read this in [English](README_en.md).*

一个将商业或开源大语言模型的api映射为OpenAI风格的api的工具，名称取自上海换乘客流最大地铁站“世纪大道”。

## **特征**
- 1、目前支持智谱清言GLM4、讯飞星火Lite、讯飞星火Pro、讯飞星火3.5Max
- 2、目前（仅）支持`/v1/chat/completions`、`/v1/models`接口

## **用法**
### 通过Releases使用（推荐）
0、前提条件：
- Jdk21
- JAVA_HOME及Path环境变量配置

1、从[Releases](https://github.com/FuturePrayer/century_avenue/releases)页面下载最新版jar构建；

2、`application.yaml`配置文件示例：（请注意，构建内默认配置文件中不包含任何api对接参数，直接运行是无法调用任何一种大模型的哦！）

```yaml
# 开启鉴权，请求头中需添加Authorization，规则与OpenAI一致，内容为"Bearer "+api-key
# 不配置此项则不检验授权，但我们强烈推荐开启
api-key: "my api-key"
# 智谱清言GLM4
glm-4:
  api-key: "glm-4 api-key"
# 讯飞星火lite
spark-lite:
  app_id: "spark-lite app_id"
  api-secret: "spark-lite api-secret"
  api-key: "spark-lite api-key"
# 讯飞星火pro
spark-pro:
  app_id: "spark-pro app_id"
  api-secret: "spark-pro api-secret"
  api-key: "spark-pro api-key"
# 讯飞星火3.5Max
spark35-max:
  app_id: "spark35-max app_id"
  api-secret: "spark35-max api-secret"
  api-key: "spark35-max api-key"
# 阿里巴巴Qwen-Long
qwen-long:
  api-key: "qwen-long api-key"

```

3、执行`java -jar`命令
```bash
java -jar century_avenue-0.0.1-SNAPSHOT.jar --spring.config.import=file:/path/to/application.yaml

```
注意将jar名称修改为你下载的构建的名称；

4、访问 `http://localhost:4523/v1/models` ，即可看到当前支持的大模型列表 **（注意：仅有配置了全部对接参数的大模型才会显示在模型列表中）**。

### 从源代码构建
0、前提条件：
- Jdk21
- Maven
- JAVA_HOME及Path环境变量配置

1、
```bash
git clone https://github.com/FuturePrayer/century_avenue.git
```
2、
```bash
cd century_avenue
```
3、
```
mvn package
```

## **局限性**
- 1、当前支持的大模型较少，本人精力、财力优先，无法购买每一款商业大模型或运行每一款开源大模型用来做适配，故欢迎各位大佬们参与贡献；
- 2、当前除了GLM4（存疑，理论可行，未经验证）外，暂不支持函数调用。

## **参与贡献**
- 1、开源大模型的实现类请放置到`cn.miketsu.century_avenue.service.open`包中，商业闭源大模型请放置到`cn.miketsu.century_avenue.service.closed`包中；
- 2、代码合并需提供完整测试报告，格式不限，但内容需要包括流式调用、非流式调用、配置了对接参数的情况下模型列表接口的返回以及未配置时是否可用。

## **鸣谢**
- [@FuturePrayer](https://github.com/FuturePrayer)：sihuangwlp@petalmail.com
- （还有你）
