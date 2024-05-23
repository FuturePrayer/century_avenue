# **Century_Avenue**
A tool that maps the API of commercial or open-source large language models to OpenAi-style APIs, named after the "Century Avenue" subway station in Shanghai, which has the highest passenger flow among all stations.

## **Features**
Currently, it supports the following AI language models: iFLYTEK GLM4, iFLYTEK Spark Lite, iFLYTEK Spark Pro, and iFLYTEK Spark 3.5Max
Currently, only the `/v1/chat/completions` and `/v1/models` interfaces are supported

## **Usage**
### Using Releases (recommended)
0. Prerequisite:
- Jdk21
- JAVA_HOME and Path environment variable configuration

1. Download the latest jar build from the [Releases](https://github.com/FuturePrayer/century_avenue/releases) page;

2. Example of the `application.yaml` configuration file: (Please note that the default configuration file in the build does not contain any API integration parameters, and running it directly will not be able to call any large models!)

```yaml
# zhipuai GLM4
glm-4:
  api-key: "glm-4 api-key"
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
- Currently, there are few large models supported, and my energy and financial resources are prioritized. I cannot purchase every commercial large model or run every open source large model for adaptation. Therefore, I welcome everyone to participate and contribute;
- Currently, except for GLM4 (doubtful, theoretically possible, not verified), function calls are not supported.

## **Participation and Contribution**
- The implementation class of the open source large model should be placed in the `cn.miketsu.century_avenue.service.open` package, and the commercial closed source large model should be placed in the `cn.miketsu.century_avenue.service.closed` package;
- For code merging, a complete test report is required, in any format, but the content needs to include streaming calls, non-streaming calls, the return of the model list interface when configured with docking parameters, and whether it is available when not configured.

## **Acknowledgements**
- [@FuturePrayer](https://github.com/FuturePrayer): sihuangwlp@petalmail.com
- (And you)