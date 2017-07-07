package com.ai.ips.common.docker;

import com.ai.ips.common.msg.IpsResult;
import com.ai.ips.common.msg.ResultCode;
import com.ai.ips.common.rest.response.CommonRspMsg;
import com.ai.ips.common.util.HttpClientUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.AuthConfig;

import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Desc: 仓库访问接口
 * User: TangBingbing
 * NT: tangbb/70288
 * Date：2017/7/7
 * Version: 1.0
 * Created by IntelliJ IDEA.
 * To change this template use File | Settings | File and Code Templates.
 */
public class DockerRegistryClient {

    private static final Logger LOG = LoggerFactory.getLogger(DockerRegistryClient.class);
    private static final String DefaultDockerServerUrl = "unix:///var/run/docker.sock";

    private static DockerClient dockerClient;


    private DockerRegistryClient() {
        dockerClient = initSimple(DefaultDockerServerUrl);
    }

    public static DockerClient initSimple(String serverUrl) {
        DockerClient dockerClient = DockerClientBuilder.getInstance(serverUrl).build();
        return dockerClient;
    }


    /**
     * 判断仓库是否能ping通
     */
    public static boolean checkRegistry(Registry registryInfo) {
        return registryInfo.checkStatus();

    }

    /**
     * 从仓库获取镜像详细信息
     */
    public static ImageInfo getImageInfo(Registry registry, String imageName, String tag) {
        return getImageInfo(registry, imageName, tag, registry.getAuthConfig());
    }


    /**
     * 从仓库获取镜像详细信息
     */
    public static ImageInfo getImageInfo(Registry registry, String imageName, String tag, AuthConfig authConfig) {
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setImageName(imageName);
        imageInfo.setTag(tag);
        String uri = registry.getUri() + imageName + "/manifests/" + tag;
        CommonRspMsg crm = null;
        if (authConfig != null && authConfig.getUsername() != null && !authConfig.getUsername().equals("")) {
            crm = HttpClientUtil.executeGetRequest(uri, authConfig.getUsername(), authConfig.getPassword());
        } else {
            crm = HttpClientUtil.executeGetRequest(uri);
        }
        if (crm.getResultCode() == ResultCode.ERC_SUCCESS.getValue()) {
            String result = crm.getResult();
            //获取history中镜像历史信息
            JSONObject resultJson = JSONObject.parseObject(result);
            String history = resultJson.getJSONArray("history").getString(0);
            //v1仓库兼容的具体内容 ，包含id，version，size等信息
            JSONObject v1 = JSONObject.parseObject(history);
            String imageStr = v1.getString("v1Compatibility");
            JSONObject imageJson = JSONObject.parseObject(imageStr);
            //获取id
            String imageId = imageJson.getString("id");
            imageInfo.setImageId(imageId);
        }
        return imageInfo;
    }

    public static IpsResult pullImage(Registry registry, String imageName, String imageTag) {
        return pullImage(dockerClient, registry, imageName, imageTag);
    }

    /**
     * 下载指定仓库的指定镜像  pull image
     *
     * @param dockerClient 客户端连接
     * @param registry     仓库信息
     * @param imageName    镜像名字 eg:tomcat
     * @param imageTag     tag eg:1.0
     */
    public static IpsResult pullImage(DockerClient dockerClient, Registry registry, String imageName, String imageTag) {
        IpsResult result = new IpsResult();
        String registryImageURI = registry.getRegistryImageURI(imageName);
        AuthConfig authConfig = registry.getAuthConfig();
        if (authConfig == null) {
            authConfig = new AuthConfig();
        }
        LOG.info("############### start pull the image {}:{} ###############", registryImageURI, imageTag);
        System.out.println("start pull the image " + registryImageURI + " imageTag =" + imageTag);
        PullImageResultCallback res = dockerClient.pullImageCmd(registryImageURI).withTag(imageTag)
                .withAuthConfig(authConfig).exec(new PullImageResultCallback());
        try {
            res.awaitSuccess();
        } catch (DockerClientException e) {
            LOG.debug("############### pull image failed , {} ###############", e.getMessage());
            result.setResult(false);
            result.setErrorMsg(e.getMessage());
            return result;
        }
        LOG.debug("############### pull image success ###############");
        result.setResult(true);
        return result;
    }

}
