package cn.miketsu.century_avenue.util;

import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * HTTP请求工具类
 *
 * @author sihuangwlp
 * @date 2024/5/27
 * @since 1.0.0
 */
public final class HttpUtil {

    private HttpMethod method;

    private String url;

    private Map<String, String[]> header;

    private Object body;

    /**
     * 新建一个get请求
     *
     * @return HttpUtil操作对象
     * @author sihuangwlp
     * @date 2024/5/27
     * @since 1.0.0
     */
    public static HttpUtil get() {
        HttpUtil httpUtil = new HttpUtil();
        httpUtil.setMethod(HttpMethod.GET);
        return httpUtil;
    }

    /**
     * 新建一个get请求
     *
     * @param url 请求地址
     * @return HttpUtil操作对象
     * @author sihuangwlp
     * @date 2024/5/27
     * @since 1.0.0
     */
    public static HttpUtil get(String url) {
        HttpUtil httpUtil = get();
        return httpUtil.url(url);
    }

    /**
     * 新建一个post请求
     *
     * @return HttpUtil操作对象
     * @author sihuangwlp
     * @date 2024/5/27
     * @since 1.0.0
     */
    public static HttpUtil post() {
        HttpUtil httpUtil = new HttpUtil();
        httpUtil.setMethod(HttpMethod.POST);
        return httpUtil;
    }

    /**
     * 新建一个post请求
     *
     * @param url 请求地址
     * @return HttpUtil操作对象
     * @author sihuangwlp
     * @date 2024/5/27
     * @since 1.0.0
     */
    public static HttpUtil post(String url) {
        HttpUtil httpUtil = post();
        return httpUtil.url(url);
    }

    /**
     * 设置请求地址
     *
     * @param url 请求地址
     * @return HttpUtil操作对象
     * @author sihuangwlp
     * @date 2024/5/27
     * @since 1.0.0
     */
    public HttpUtil url(String url) {
        this.setUrl(url);
        return this;
    }

    /**
     * 设置请求头
     *
     * @param headerName   请求头名称
     * @param headerValues 请求头值
     * @return HttpUtil操作对象
     * @author sihuangwlp
     * @date 2024/5/27
     * @since 1.0.0
     */
    public HttpUtil header(String headerName, String... headerValues) {
        if (this.getHeader() == null) {
            this.setHeader(new HashMap<>());
        }
        this.getHeader().put(headerName, headerValues);
        return this;
    }

    /**
     * 设置请求体（仅post有效）
     *
     * @param body 请求体
     * @return HttpUtil操作对象
     * @author sihuangwlp
     * @date 2024/5/27
     * @since 1.0.0
     */
    public HttpUtil body(Object body) {
        this.setBody(body);
        return this;
    }

    /**
     * 获取响应
     *
     * @param respClass 响应参数类型
     * @param <T>       响应参数类型
     * @return 响应内容
     * @author sihuangwlp
     * @date 2024/5/28
     * @since 1.0.0
     */
    public <T> T resp(Class<T> respClass) {
        RestClient build = RestClient.builder().build();
        if (this.getMethod() == HttpMethod.GET) {
            RestClient.RequestHeadersUriSpec<?> spec = build.get();
            RestClient.RequestHeadersSpec<?> uriSpec = spec.uri(this.getUrl());
            if (this.getHeader() != null) {
                for (Map.Entry<String, String[]> entry : this.getHeader().entrySet()) {
                    uriSpec.header(entry.getKey(), entry.getValue());
                }
            }
            return uriSpec.retrieve().body(respClass);
        } else if (this.method == HttpMethod.POST) {
            RestClient.RequestBodyUriSpec spec = build.post();
            RestClient.RequestHeadersSpec<?> uriSpec = spec.uri(this.getUrl());
            if (this.getHeader() != null) {
                for (Map.Entry<String, String[]> entry : this.getHeader().entrySet()) {
                    uriSpec.header(entry.getKey(), entry.getValue());
                }
            }
            spec.body(this.getBody());
            return uriSpec.retrieve().body(respClass);
        } else {
            throw new RuntimeException("unsupported http method");
        }
    }

    /**
     * 获取响应
     *
     * @param respClass       响应参数类型
     * @param convertFunction 将响应参数转换为指定类型
     * @param <T>             直接接收响应内容的参数类型
     * @param <V>             转换后的类型
     * @return 响应内容
     * @author sihuangwlp
     * @date 2024/5/28
     * @since 1.0.0
     */
    public <T, V> V resp(Class<T> respClass, Function<T, V> convertFunction) {
        T resp = resp(respClass);
        return convertFunction.apply(resp);
    }

    protected HttpMethod getMethod() {
        return method;
    }

    protected void setMethod(HttpMethod method) {
        this.method = method;
    }

    protected String getUrl() {
        return url;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    protected Map<String, String[]> getHeader() {
        return header;
    }

    protected void setHeader(Map<String, String[]> header) {
        this.header = header;
    }

    protected Object getBody() {
        return body;
    }

    protected void setBody(Object body) {
        this.body = body;
    }
}
