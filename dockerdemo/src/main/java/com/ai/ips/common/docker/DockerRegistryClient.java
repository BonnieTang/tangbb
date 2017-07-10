package com.ai.ips.common.docker;

import com.ai.ips.common.msg.IpsResult;
import com.ai.ips.common.msg.ResultCode;
import com.ai.ips.common.rest.response.CommonRspMsg;
import com.ai.ips.common.util.HttpClientUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.AuthConfig;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 获取单个镜像的所有信息
     */
    public static List<ImageInfo> getImageTagsList(Registry registry, String imageName) {
        AuthConfig authConfig = registry.getAuthConfig();
        String uri = registry.getTagsListUri(imageName);
        CommonRspMsg crm = null;
        List<ImageInfo> list = new ArrayList<ImageInfo>();
        if (authConfig != null && authConfig.getUsername() != null && !authConfig.getUsername().equals("")) {
            crm = HttpClientUtil.executeGetRequest(uri, authConfig.getUsername(), authConfig.getPassword());
        } else {
            crm = HttpClientUtil.executeGetRequest(uri);
        }
        if (crm.getResultCode() == ResultCode.ERC_SUCCESS.getValue()) {
            String taginfo = crm.getResult();
            JSONObject json = JSONObject.parseObject(taginfo);
            JSONArray array = json.getJSONArray("tags");
            if (array != null && array.size() > 0) {
                for (int i = 0; i < array.size(); i++) {
                    String tag = array.getString(i);
                    ImageInfo image = new ImageInfo();
                    image.setImageName(imageName);
                    image.setTag(tag);
                    list.add(image);
                }
            }
        }
        return list;
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


    /**
     * 使用本机启动的dorker client
     */
    public static IpsResult pushImage(Registry registry, String imageName, String imageTag, boolean delFlag) {
        return pushImage(dockerClient, registry, imageName, imageTag, delFlag);
    }

    /**
     * 使用本机启动的dorker client，默认上传完成后删除本地镜像
     */
    public static IpsResult pushImage(Registry registry, String imageName, String imageTag) {
        return pushImage(dockerClient, registry, imageName, imageTag, true);
    }

    /**
     * 上传本地镜像到指定私有镜像仓库 push image
     * 上传完成后默认删除本地镜像
     */
    public static IpsResult pushImage(DockerClient dockerClient, Registry registry, String imageName, String imageTag) {
        return pushImage(dockerClient, registry, imageName, imageTag, true);
    }

    /**
     * 上传本地镜像到指定私有镜像仓库 push image
     *
     * @param dockerClient 指定操作客户端
     * @param registry     仓库信息
     * @param imageName    镜像信息
     * @param imageTag     镜像标签
     * @param delFlag      上传完成后是否删除本地镜像
     */
    public static IpsResult pushImage(DockerClient dockerClient, Registry registry, String imageName, String imageTag, boolean delFlag) {
        AuthConfig authConfig = registry.getAuthConfig();
        String imageRegisryName = registry.getImagePushUri(imageName, imageTag); //仓库名称192.168.243.2:5000/helloword
        LOG.info("###############start push the image {}:{} ###############", imageRegisryName, imageTag);
        IpsResult result = new IpsResult();
        if (authConfig == null) {
            authConfig = new AuthConfig();
        }
        PushImageResultCallback hpsRes = dockerClient.pushImageCmd(imageRegisryName).withTag(imageTag)
                .withAuthConfig(authConfig).exec(new PushImageResultCallback());
        try {
            hpsRes.awaitSuccess();
        } catch (DockerClientException e) {
            LOG.error("###############push image failed , {} ##############", e.getMessage());
            result.setResult(false);
            result.setErrorMsg(e.getMessage());
            return result;
        } finally {
            if (delFlag) {
                deleteImageFromLocal(imageRegisryName);
            }
        }
        LOG.info("###############push image success ###############");
        result.setResult(true);
        return result;
    }

    /**
     * 查询指定主机本地所有Image信息
     */
    public static List<Image> listImageInfoFromLocal(DockerClient dockerClient) {
        try {
            List<Image> imageList = dockerClient.listImagesCmd().exec();
            for (Image image : imageList) {
                LOG.info("Image Info : id = {} repotags = {} ", image.getId(), image.getRepoTags());
            }
            return imageList;
        } catch (Exception e) {
            LOG.error("{}", e.getMessage());
            return new ArrayList<Image>();
        }
    }

    /**
     * 查询本地所有Image信息
     */
    public static List<Image> listImageInfoFromLocal() {
        return listImageInfoFromLocal(dockerClient);
    }

    /**
     * 查询指定本地镜像信息
     */
    public static Image listImageInfoFromLocal(DockerClient dockerClient, String imageId) {
        try {
            List<Image> imageList = dockerClient.listImagesCmd().exec();
            for (Image image : imageList) {
                LOG.debug("listImageInfoFromLocal:imageId:" + imageId.substring(0, 12));
                LOG.debug("listImageInfoFromLocal:imageId:" + image.getId().substring(0, 12));
                if (image.getId().equals(imageId) || (imageId.substring(0, 12).equals(image.getId().substring(0, 12)))) {
                    return image;
                }
            }
        } catch (Exception e) {
            LOG.error("{}", e.getMessage());
        }
        return null;
    }

    /**
     * 删除本地某个镜像 若存在容器依赖，则不删除
     * @param imageId
     * @return
     */
    public static boolean deleteImageFromLocal(String imageId) {
        return deleteImageFromLocal(dockerClient, imageId);
    }

    /**
     * 删除本地某个镜像 若存在容器依赖，则不删除
     * 如果直接删除存在一个问题，docker是先删除tag 再删除文件：若此imageId只有一个tag，则会导致产生<none>：<none>的镜像
     *
     * @param imageId 镜像imageId  name:tag或id
     * @return false，删除失败；true，删除成功
     */
    public static boolean deleteImageFromLocal(DockerClient dockerClient, String imageId) {
        boolean containerRelyflag = false; //判断镜像是否已经开启服务
        try {
            List<Container> containerList = dockerClient.listContainersCmd().withShowAll(true).exec();
            for (Container container : containerList) {
                if (imageId.equals(container.getImage())) {
                    containerRelyflag = true;
                    LOG.warn("unable to remove the image \"" + imageId + "\" as container \"" + container.getId() + "\" is using this image");
                    break;
                }
            }
            if (!containerRelyflag) {
                dockerClient.removeImageCmd(imageId).withForce(true).exec();
            }
            LOG.info("Delete the local image {} success. ",imageId);
            return true;
        } catch (Exception e) {
            LOG.error("Delete local image {} failed {}", imageId, e.getMessage());
            return false;
        }
    }
}
