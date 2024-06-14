# **Century_Avenue**
A tool that maps the API of commercial or open-source large language models to OpenAI-style APIs, named after the "Century Avenue" subway station in Shanghai, which has the highest passenger flow among all stations.

## **Features**
- Currently, it supports the following AI language models: zhipuai GLM4 Series, iFLYTEK Spark Lite, iFLYTEK Spark Pro, iFLYTEK Spark 3.5Max, Qwen Long, Baidu Ernie Series
- Currently, only the `/v1/chat/completions` and `/v1/models` interfaces are supported
- Support forwarding to the specified URL via the aforementioned two interfaces

## **Usage**
### Using Releases (recommended)
0. Prerequisite:
- Jdk21
- JAVA_HOME and Path environment variable configuration

1. Download the latest jar build from the [Releases](https://github.com/FuturePrayer/century_avenue/releases) page;

2. Example of the `application.yaml` configuration file: (Please note that the following parameters are not required, but the default configuration file in the build does not include any API docking parameters. Running directly will not be able to call any large model!)

```yaml
# Enable authentication, add Authorization to the request header, and follow the same rules as OpenAI, with the content being "Bearer"+api-key
# If this option is not configured, authorization will not be verified, but we strongly recommend enabling it
api-key: "my api-key"
# Forward requests to any large models that comply with the OpenAI API style
openai:
  models:
    - apiKey: "my key"
      model: "my model"
      baseUrl: "https://api.openai.com"
    - apiKey: "my key"
      model: "my model"
      baseUrl: "https://dashscope.aliyuncs.com/compatible-mode"
# zhipuai GLM4
glm-4:
  api-key: "glm-4 api-key"
  subModels:
    - "glm-4-air"
    - "glm-4-airx"
    # and so on
# iFlytek Spark Lite
spark-lite:
  app_id: "spark-lite app_id"
  api-secret: "spark-lite api-secret"
  api-key: "spark-lite api-key"
# iFlytek Spark Pro
spark-pro:
  app_id: "spark-pro app_id"
  api-secret: "spark-pro api-secret"
  api-key: "spark-pro api-key"
# iFlytek Spark3.5 Max
spark35-max:
  app_id: "spark35-max app_id"
  api-secret: "spark35-max api-secret"
  api-key: "spark35-max api-key"
# Alibaba Qwen-Long
qwen-long:
  api-key: "qwen-long api-key"
# Baidu ERNIE-Tiny-8K
ernie-tiny-8k:
  api-key: "ernie-tiny-8k api-key"
  secret-key: "ernie-tiny-8k secret-key"
# Baidu ERNIE-Lite-8K-0308
ernie-lite-8k:
  api-key: "ernie-lite-8k api-key"
  secret-key: "ernie-lite-8k secret-key"
# Baidu ERNIE-Speed-8K
ernie-speed:
  api-key: "ernie-speed api-key"
  secret-key: "ernie-speed secret-key"
# Baidu ERNIE-Speed-128K
ernie-speed-128k:
  api-key: "ernie-speed-128k api-key"
  secret-key: "ernie-speed-128k secret-key"
# Baidu ERNIE-3.5-8K-Preview
ernie-3.5-8k-preview:
  api-key: "ernie-3.5-8k-preview api-key"
  secret-key: "ernie-3.5-8k-preview secret-key"
# Baidu ERNIE-4.0-8K-Preview
ernie-4.0-8k-preview:
  api-key: "ernie-4.0-8k-preview api-key"
  secret-key: "ernie-4.0-8k-preview secret-key"
# The alias mapping of large models can be configured multiple times, and multiple aliases can point to the same available large model. All available models and their corresponding aliases will be displayed in /v1/models
century-avenue:
  model-mapping:
    gpt3.5: "spark-lite"
    gpt4: "spark-lite"
    gpt4o: "spark-pro"

```

3. Execute the command `java -jar`
```bash
java -jar century_avenue-0.0.1-SNAPSHOT.jar --spring.config.import=file:/path/to/application.yaml

```
Note that the jar name should be modified to the name of the build you downloaded;

4. Visit `http://localhost:4523/v1/models` to see the list of currently supported large models **(Note: Only large models with all docking parameters configured will be displayed in the model list)** .

### Build from source code
0. Prerequisite:
- Jdk21
- Maven
- JAVA_HOME and Path environment variable configuration

1
```bash
git clone https://github.com/FuturePrayer/century_avenue.git
```
2
```bash
cd century_avenue
```
3
```
mvn package
```

## **Limitations**
- The current support for large models is limited. Due to my limited energy and financial resources, I am unable to purchase every commercial large model or run every open-source large model for adaptation. Therefore, I welcome everyone to contribute;
- Currently, function calls are not supported.

## **Participation and Contribution**
- The implementation class of the open source large model should be placed in the `cn.miketsu.century_avenue.service.open` package, and the commercial closed source large model should be placed in the `cn.miketsu.century_avenue.service.closed` package;
- For code merging, a complete test report is required, in any format, but the content needs to include streaming calls, non-streaming calls, the return of the model list interface when configured with docking parameters, and whether it is available when not configured.

## **Acknowledgements**
- [@FuturePrayer](https://github.com/FuturePrayer): sihuangwlp@petalmail.com
- (And you)