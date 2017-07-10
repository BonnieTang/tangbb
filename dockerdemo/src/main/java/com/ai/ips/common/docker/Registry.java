package com.ai.ips.common.docker;

import com.ai.ips.common.msg.ResultCode;
import com.ai.ips.common.rest.response.CommonRspMsg;
import com.ai.ips.common.util.HttpClientUtil;
import com.github.dockerjava.api.model.AuthConfig;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Desc: 仓库基本信息
 * User: TangBingbing
 * NT: tangbb/70288
 * Date：2017/7/7
 * Version: 1.0
 * Created by IntelliJ IDEA.
 * To change this template use File | Settings | File and Code Templates.
 */
public class Registry {
    private static final Logger LOG = LoggerFactory.getLogger(Registry.class);

    private final static int DefaultPort = 5000;
    private final static String DefaultVersion = "v2";
    private String host;
    private int port = DefaultPort;
    private String username = "";
    private String password;
    private String email = "";
    private String version = DefaultVersion;
    ;

    /**
     * 无密码访问的仓库,默认端口5000
     */
    public Registry(String host) {
        this.host = host;
    }

    /**
     * 无密码访问的仓库
     *
     * @param host 10.1.2.4
     * @param port 5000
     */
    public Registry(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 有密码访问仓库，默认端口5000
     */
    public Registry(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    /**
     * 有密码访问的仓库
     */
    public Registry(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * 指定版本的有密码访问的仓库
     */
    public Registry(String host, int port, String username, String password, String version) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.version = version;
    }

    /**
     * 获取本仓库镜像的URL
     * @param imageName
     * @return 10.1.234.246:5000/tomcat
     */
    public String getRegistryImageURI(String imageName) {
        return host + ":" + port + "/" + imageName;
    }

    /**
     * 获取指定镜像的所有TAG信息
     *
     * @return 完整URL  http://10.1.245.236:5000/v2/tomcat/tags/list/
     */
    public String getTagsListUri(String imageName) {
        return "http://" + host + ":" + port + "/" + version + "/" + imageName + "/tags/list/";
    }

    /**
     * 获取待传镜像的URL串
     * @param imageName
     * @param imageTag
     * @return  10.1.245.236:5000/tomcat:8.0
     */
    public String getImagePushUri(String imageName,String imageTag)
    {
        return getRegistryImageURI(imageName) + ":" + imageTag;
    }
    /**
     * 获取仓库URI串
     * @return  http://10.1.245.236:5000/v2/
     */
    public String getUri() {
        return "http://" + host + ":" + port + "/" + version + "/";
    }

    /**
     * 检查仓库状态
     *
     * @return true false
     */
    public boolean checkStatus() {
        CommonRspMsg crm = null;
        String uri = getUri();
        long start = System.currentTimeMillis();
        if (username == null || "".equals(username)) {
            crm = HttpClientUtil.executeGetRequest(uri, 1000, 1000, 1000);
        } else {
            crm = HttpClientUtil.executeGetRequest(uri, username, password, 1000, 1000, 1000);
        }
        System.out.println("查询仓库状态共耗时" + (System.currentTimeMillis() - start) + "ms");
        if (crm.getResultCode() == ResultCode.ERC_SUCCESS.getValue()) {
            return true;
        } else {
            LOG.error("#########check the registry:{} failed,the result is:{}", uri, crm.getResult());
            return false;
        }
    }


    public String auth() {
        String tmp = username + ":" + password;
        return Base64.encodeBase64String(tmp.getBytes());
    }

    /**
     * 获取鉴权配置，username为空，则返回new AuthConfig()
     */
    public AuthConfig getAuthConfig() {
        LOG.debug("the username is {} ,the password is {}", username, password);
        AuthConfig authConfig = new AuthConfig();
        if (username != null && !"".equals(username)) {
            authConfig.withUsername(username);
            authConfig.withPassword(password);
            authConfig.withRegistryAddress(getUri());
            authConfig.withEmail(email);
        }
        return authConfig;
    }


    @Override
    public String toString() {
        return "Registry{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
