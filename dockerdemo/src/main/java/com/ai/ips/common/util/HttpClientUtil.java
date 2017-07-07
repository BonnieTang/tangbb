package com.ai.ips.common.util;

import com.ai.ips.common.msg.ResultCode;
import com.ai.ips.common.rest.response.CommonRspMsg;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by liuwj on 2015/10/24.
 * http客户端工具
 */
public class HttpClientUtil {

    private static Logger LOG = LoggerFactory.getLogger(HttpClientUtil.class);

    /**
     * httpClient，主要用来操作访问marathon等
     */
    private static CloseableHttpClient httpClient;

    static {
        // 初始化多线程的链接管理器，默认为10个链接，并构造httpClient
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(20);
        httpClient = HttpClients.custom()
                                .setConnectionManager(cm)
                                .addInterceptorFirst(new HttpRequestInterceptor() {
                                    @Override
                                    public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
                                        HttpClientContext httpClientContext = (HttpClientContext) httpContext;
                                        AuthState authState = httpClientContext.getTargetAuthState();
                                        HttpHost targetHost = httpClientContext.getTargetHost();
                                        AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
                                        CredentialsProvider credsProvider = httpClientContext.getCredentialsProvider();
                                        Credentials creds = credsProvider.getCredentials(authScope);
                                        if (creds != null) {
                                            authState.update(new BasicScheme(), creds);
                                        }
                                    }
                                })
                                .build();
    }

    /**
     * 构造httpClient上下文，为执行http操作时配置鉴权信息
     * @param username
     * @param password
     * @return
     */
    private static HttpClientContext createHttpClientContext(String username, String password) {

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(username, password));
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);

        return context;
    }

    /**
     * 执行http put请求
     *
     * @param uri  要访问的uri
     * @param body 要访问的消息体部分
     * @return 返回响应消息
     */
    public static CommonRspMsg executePutRequest(String uri, String body) {
        // 构造http put请求
        HttpPut request = new HttpPut(uri);
        if (StringUtils.isNotEmpty(body)) {
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON.withCharset("UTF-8")));
        }

        return executeRequest(request);
    }

    /**
     * 执行http put请求
     *
     * @param uri  要访问的uri
     * @param body 要访问的消息体部分
     * @return 返回响应消息
     */
    public static CommonRspMsg executePutRequest(String uri, String body, String username, String password) {
        // 构造http put请求
        HttpClientContext context = createHttpClientContext(username, password);
        HttpPut request = new HttpPut(uri);
        if (StringUtils.isNotEmpty(body)) {
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON.withCharset("UTF-8")));
        }

        return executeRequest(request, context);
    }

    /**
     * 执行http put请求
     *
     * @param uri 要访问的uri
     * @return 返回响应消息
     */
    public static CommonRspMsg executePutRequest(String uri) {
        return executePutRequest(uri, "");
    }

    /**
     * 执行http put请求
     *
     * @param uri 要访问的uri
     * @return 返回响应消息
     */
    public static CommonRspMsg executePutRequest(String uri, String username, String password) {
        return executePutRequest(uri, "", username, password);
    }

    /**
     * 执行http post请求
     *
     * @param uri         要访问的uri
     * @param body        要访问的消息体部分
     * @param contentType 内容类型
     * @return 返回响应消息
     */
    public static CommonRspMsg executePostRequest(String uri, String body, ContentType contentType) {
        // 构造http post请求
        HttpPost request = new HttpPost(uri);
        if (StringUtils.isNotEmpty(body)) {
            request.setEntity(new StringEntity(body, contentType));
        }

        return executeRequest(request);
    }

    /**
     * 执行http post请求
     *
     * @param uri         要访问的uri
     * @param body        要访问的消息体部分
     * @param contentType 内容类型
     * @return 返回响应消息
     */
    public static CommonRspMsg executePostRequest(String uri, String body, ContentType contentType, String username, String password) {
        // 构造http post请求
        HttpClientContext context = createHttpClientContext(username, password);
        HttpPost request = new HttpPost(uri);
        if (StringUtils.isNotEmpty(body)) {
            request.setEntity(new StringEntity(body, contentType));
        }

        return executeRequest(request, context);
    }

    /**
     * 执行http post请求
     *
     * @param uri         要访问的uri
     * @return 返回响应消息
     */
    public static CommonRspMsg executePostRequest(String uri) {
        return executePostRequest(uri, "", ContentType.TEXT_PLAIN);
    }

    /**
     * 执行http post请求
     *
     * @param uri         要访问的uri
     * @return 返回响应消息
     */
    public static CommonRspMsg executePostRequest(String uri, String username, String password) {
        return executePostRequest(uri, "", ContentType.TEXT_PLAIN, username, password);
    }

    /**
     * 执行http delete请求
     *
     * @param uri 要访问的uri
     * @return 返回响应消息
     */
    public static CommonRspMsg executeDeleteRequest(String uri) {
        // 构造http delete请求
        HttpDelete request = new HttpDelete(uri);

        return executeRequest(request);
    }

    public static CommonRspMsg executeDeleteRequest(String uri, String username, String password) {
        // 构造http delete请求
        HttpClientContext context = createHttpClientContext(username, password);
        HttpDelete request = new HttpDelete(uri);

        return executeRequest(request, context);
    }

    /**
     * 执行http get请求
     *
     * @param uri 要访问的uri
     * @return 返回响应消息
     */
    public static CommonRspMsg executeGetRequest(String uri) {
        // 构造http get请求
        HttpGet request = new HttpGet(uri);

        return executeRequest(request);
    }

    /**
     * 执行http get请求
     *
     * @param uri 要访问的uri
     * @return 返回响应消息
     */
    public static CommonRspMsg executeGetRequest(String uri, String username, String password) {
        // 构造http get请求
        HttpClientContext context = createHttpClientContext(username, password);
        HttpGet request = new HttpGet(uri);

        return executeRequest(request, context);
    }

    /**
     * 执行http get请求
     *
     * @param uri 要访问的uri
     * @return 返回响应消息
     */
    public static CommonRspMsg executeGetRequest(String uri, String acceptValue) {
        // 构造http get请求
        HttpGet request = new HttpGet(uri);
        request.setHeader("Accept", acceptValue);

        return executeRequest(request);
    }

    /**
     * 执行http get请求
     *
     * @param uri 要访问的uri
     * @return 返回响应消息
     */
    public static CommonRspMsg executeGetRequest(String uri, String acceptValue, String username, String password) {
        // 构造http get请求
        HttpClientContext context = createHttpClientContext(username, password);
        HttpGet request = new HttpGet(uri);
        request.setHeader("Accept", acceptValue);

        return executeRequest(request, context);
    }


    /**
     * 执行http请求
     *
     * @param request http请求
     * @return 返回通用响应消息
     */
    private static CommonRspMsg executeRequest(HttpRequestBase request) {
        // 执行请求
        try {
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {//如果状态码为200,就是正常返回
                return new CommonRspMsg(ResultCode.ERC_SUCCESS.getValue(), EntityUtils.toString(response.getEntity()));
            } else {
                // 打印状态码和错误提示
                LOG.warn("execute request failed, status line is {}", response.getStatusLine());
                return new CommonRspMsg(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            LOG.warn("{}", e);
        } catch (IOException e) {
            LOG.warn("{}", e);
        } finally {
            // 释放当前的连接，使其回归线程池可以继续复用
            request.releaseConnection();
        }
        return new CommonRspMsg(ResultCode.ERC_FAILED.getValue(), "execute request failed");
    }

    /**
     * 执行http请求，附带鉴权信息
     *
     * @param request http请求
     * @return 返回通用响应消息
     */
    private static CommonRspMsg executeRequest(HttpRequestBase request, HttpClientContext httpClientContext) {
        // 执行请求
        try {
            HttpResponse response = httpClient.execute(request, httpClientContext);
            if (response.getStatusLine().getStatusCode() == 200) {//如果状态码为200,就是正常返回
                return new CommonRspMsg(ResultCode.ERC_SUCCESS.getValue(), EntityUtils.toString(response.getEntity()));
            } else {
                // 打印状态码和错误提示
                LOG.warn("execute request failed, status line is {}", response.getStatusLine());
                return new CommonRspMsg(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            LOG.warn("{}", e);
        } catch (IOException e) {
            LOG.warn("{}", e);
        } finally {
            // 释放当前的连接，使其回归线程池可以继续复用
            request.releaseConnection();
        }
        return new CommonRspMsg(ResultCode.ERC_FAILED.getValue(), "execute request failed");
    }
    
    /**
     * 执行http请求
     *
     * @param request http请求
     * @return 返回通用响应消息
     */
    private static CommonRspMsg executeRequst(HttpRequestBase request) {
        // 执行请求
        try {
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {//如果状态码为200,就是正常返回
                return new CommonRspMsg(ResultCode.ERC_SUCCESS.getValue(), EntityUtils.toString(response.getEntity()));
            } else {
                // 打印状态码和错误提示
                LOG.warn("execute request failed, status line is {}", response.getStatusLine());
                return new CommonRspMsg(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity()));
            }
        } catch (ClientProtocolException e) {
            LOG.warn("{}", e);
        } catch (IOException e) {
            LOG.warn("{}", e);
        } finally {
            // 释放当前的连接，使其回归线程池可以继续复用
            request.releaseConnection();
        }
        return new CommonRspMsg(ResultCode.ERC_FAILED.getValue(), "execute request failed");
    }
    
    /**
     * 执行Http Get请求，可指定超时时间
     * @param uri
     * @param connectTimeout 连接超时时间，单位毫秒
     * @param connectionRequestTimeout 请求超时时间，单位毫秒
     * @param socketTimeout 数据接收超时时间，单位毫秒
     * @return
     */
    public static CommonRspMsg executeGetRequest(String uri, int connectTimeout,
                                                 int connectionRequestTimeout, int socketTimeout) {
        // 构造http get请求
        HttpGet request = new HttpGet(uri);

        // 设置连接超时
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout).setSocketTimeout(socketTimeout).build();
        request.setConfig(requestConfig);
        
        return executeRequst(request);
    }
    
    /**
     * 执行Http Get请求，可指定超时时间
     * @param uri
     * @param connectTimeout 连接超时时间，单位毫秒
     * @param connectionRequestTimeout 请求超时时间，单位毫秒
     * @param socketTimeout 数据接收超时时间，单位毫秒
     * @return
     */
    public static CommonRspMsg executeGetRequest(String uri, String username, String password, int connectTimeout,
                                                 int connectionRequestTimeout, int socketTimeout) {
        // 构造http get请求
        HttpClientContext context = createHttpClientContext(username, password);
        HttpGet request = new HttpGet(uri);

        // 设置连接超时
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout).setSocketTimeout(socketTimeout).build();
        request.setConfig(requestConfig);
        
        return executeRequest(request, context);
    }
    
    /**
     * 执行Http Get请求，可指定超时时间
     * @param uri
     * @param acceptValue
     * @param connectTimeout 连接超时时间，单位毫秒
     * @param connectionRequestTimeout 请求超时时间，单位毫秒
     * @param socketTimeout 数据接收超时时间，单位毫秒
     * @return
     */
    public static CommonRspMsg executeGetRequest(String uri, String acceptValue, int connectTimeout,
                                                 int connectionRequestTimeout, int socketTimeout) {
        // 构造http get请求
        HttpGet request = new HttpGet(uri);
        request.setHeader("Accept", acceptValue);

        // 设置连接超时
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout).setSocketTimeout(socketTimeout).build();
        request.setConfig(requestConfig);
        return executeRequst(request);
    }
    
    /**
     * 执行Http post请求，可指定超时时间
     * @param uri 请求地址
     * @param body 发送数据
     * @param contentType 发送数据类型
     * @param connectTimeout 连接超时时间，单位毫秒
     * @param connectionRequestTimeout 请求超时时间，单位毫秒
     * @param socketTimeout 数据接收超时时间，单位毫秒
     * @return
     */
    public static CommonRspMsg executePostRequest(String uri, String body, ContentType contentType,
                                                  int connectTimeout, int connectionRequestTimeout, int socketTimeout) {
        // 构造http post请求
        HttpPost request = new HttpPost(uri);

        // 设置连接超时
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout).setSocketTimeout(socketTimeout).build();
        request.setConfig(requestConfig);
        
        if (StringUtils.isNotEmpty(body)) {
            request.setEntity(new StringEntity(body, contentType));
        }

        return executeRequst(request);
    }
}
