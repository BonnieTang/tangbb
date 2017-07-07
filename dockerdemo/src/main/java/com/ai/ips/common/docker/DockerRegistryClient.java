package com.ai.ips.common.docker;

import com.ai.ips.common.msg.ResultCode;
import com.ai.ips.common.rest.response.CommonRspMsg;
import com.ai.ips.common.util.HttpClientUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.AuthConfig;

import com.github.dockerjava.core.DockerClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Desc:
 * User: TangBingbing
 * NT: tangbb/70288
 * Date：2017/7/7
 * Version: 1.0
 * Created by IntelliJ IDEA.
 * To change this template use File | Settings | File and Code Templates.
 */
public class DockerRegistryClient {

    private static final Logger LOG = LoggerFactory.getLogger(DockerRegistryClient.class);
    private static final String DefaultDockerServerUrl="unix:///var/run/docker.sock";

    private static DockerClient dockerClient;


    private DockerRegistryClient(){
        dockerClient = initSimple(DefaultDockerServerUrl);
    }

    public static DockerClient initSimple(String serverUrl)
    {
        DockerClient dockerClient = DockerClientBuilder.getInstance(serverUrl).build();
        return dockerClient;
    }


    /**
     * 判断仓库是否能ping通
     * @param registryInfo
     * @return
     */
    public static boolean checkRegistry(Registry registryInfo){
       return registryInfo.checkStatus();

    }

    /**
     * 从仓库获取镜像详细信息
     * @param registry
     * @param imageName
     * @param tag
     * @return
     */
    public static ImageInfo getImageInfo(Registry registry, String imageName, String tag){
        return getImageInfo(registry,imageName,tag,registry.getAuthConfig());
    }


    /**
     * 从仓库获取镜像详细信息
     * @param registry
     * @param imageName
     * @param tag
     * @param authConfig
     * @return
     */
    public static ImageInfo getImageInfo(Registry registry, String imageName, String tag, AuthConfig authConfig){
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setImageName(imageName);
        imageInfo.setTag(tag);
        String uri = registry.getUri() + imageName + "/manifests/" + tag;
        CommonRspMsg crm  = null;
        if(authConfig != null && authConfig.getUsername() != null && !authConfig.getUsername().equals("")){
            crm = HttpClientUtil.executeGetRequest(uri, authConfig.getUsername(), authConfig.getPassword());
        }else{
            crm = HttpClientUtil.executeGetRequest(uri);
        }
        if(crm.getResultCode() == ResultCode.ERC_SUCCESS.getValue()){
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


}
