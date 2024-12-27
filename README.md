# **Century_Avenue**

*Read this in [English](README_en.md).*

一个将商业或开源大语言模型的api映射为OpenAI风格的api的工具，名称取自上海换乘客流最大地铁站“世纪大道”。

## **特征**
- 1、目前支持智谱清言GLM4系列、讯飞星火Lite、讯飞星火Pro、讯飞星火3.5Max、讯飞星火4.0Ultra、千问Long、百度Ernie系列
- 2、目前（仅）支持`/v1/chat/completions`、`/v1/models`接口
- 3、支持上述两个接口向指定url的转发

## **用法**
### 通过Releases使用（推荐）
0、前提条件：
- Jdk21
- JAVA_HOME及Path环境变量配置

1、从[Releases](https://github.com/FuturePrayer/century_avenue/releases)页面下载最新版jar构建；

2、`application.yaml`配置文件示例：（请注意，以下参数均为非必填项，但构建内默认配置文件中不包含任何api对接参数，直接运行是无法调用任何一种大模型的哦！）

```yaml
# 开启鉴权，请求头中需添加Authorization，规则与OpenAI一致，内容为"Bearer "+api-key
# 不配置此项则不检验授权，但我们强烈推荐开启
api-key: "my api-key"
# 向任意符合OpenAI API风格的大模型转发请求
# 注意：由于Uvicorn对h2和h2c的支持存在问题，本地部署的使用了fastapi的大模型建议通过Hypercorn运行，否则可能导致非流式请求报错
# 2.0.0及以上版本只会在baseUrl后补上/chat/completions，2.0.0以下版本会补上/v1/chat/completions
# apiKey支持传入多个，用半角逗号（,）隔开，系统会对相同的模型名称model进行汇总并进行轮询
openai:
  models:
    - apiKey: "my key"
      model: "my model"
      baseUrl: "https://api.openai.com/v1"
    - apiKey: "key1,key2"
      model: "my model"
      baseUrl: "https://dashscope.aliyuncs.com/compatible-mode/v1"
# 智谱清言GLM4
glm-4:
  api-key: "glm-4 api-key"
  subModels:
    - "glm-4-air"
    - "glm-4-airx"
    # 等等
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
# 讯飞星火3.5 Max
spark35-max:
  app_id: "spark35-max app_id"
  api-secret: "spark35-max api-secret"
  api-key: "spark35-max api-key"
# 讯飞星火4.0 Ultra
spark4-ultra:
  app_id: "spark4-ultra app_id"
  api-secret: "spark4-ultra api-secret"
  api-key: "spark4-ultra api-key"
# 阿里巴巴Qwen-Long
qwen-long:
  api-key: "qwen-long api-key"
# 百度ERNIE-Tiny-8K
ernie-tiny-8k:
  api-key: "ernie-tiny-8k api-key"
  secret-key: "ernie-tiny-8k secret-key"
# 百度ERNIE-Lite-8K-0308
ernie-lite-8k:
  api-key: "ernie-lite-8k api-key"
  secret-key: "ernie-lite-8k secret-key"
# 百度ERNIE-Speed-8K
ernie-speed:
  api-key: "ernie-speed api-key"
  secret-key: "ernie-speed secret-key"
# 百度ERNIE-Speed-128K
ernie-speed-128k:
  api-key: "ernie-speed-128k api-key"
  secret-key: "ernie-speed-128k secret-key"
# 百度ERNIE-3.5-8K-Preview
ernie-3.5-8k-preview:
  api-key: "ernie-3.5-8k-preview api-key"
  secret-key: "ernie-3.5-8k-preview secret-key"
# 百度ERNIE-4.0-8K-Preview
ernie-4.0-8k-preview:
  api-key: "ernie-4.0-8k-preview api-key"
  secret-key: "ernie-4.0-8k-preview secret-key"
# 大模型别名映射，可以配置多个，可以多个别名指向同一个可用的大模型，所有可用的模型及其对应的别名都会显示在/v1/models中
century-avenue:
  model-mapping:
    gpt3.5: "spark-lite"
    gpt4: "spark-lite"
    gpt4o: "spark-pro"

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
- 1、当前支持的大模型较少，本人精力、财力有限，无法购买每一款商业大模型或运行每一款开源大模型用来做适配，故欢迎各位大佬们参与贡献；
- 2、当前暂不支持函数调用。

## **参与贡献**
- 1、开源大模型的实现类请放置到`cn.miketsu.century_avenue.service.open`包中，商业闭源大模型请放置到`cn.miketsu.century_avenue.service.closed`包中；
- 2、代码合并需提供完整测试报告，格式不限，但内容需要包括流式调用、非流式调用、配置了对接参数的情况下模型列表接口的返回以及未配置时是否可用。

## **鸣谢**
- [@FuturePrayer](https://github.com/FuturePrayer)：sihuangwlp@petalmail.com
- （还有你）
