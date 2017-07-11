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

    /**
     * 指定镜像打标签 docker tag imageId helloworld:3.0
     *
     * @param imageId  源镜像id registry:tag方式
     * @param registry 镜像名称
     * @param imageTag 镜像标签
     * @param isDelete 是否删除源镜像标签
     * @return true 成功，false 失败
     */
    public static boolean tagImageByImageID(String imageId, String registry, String imageTag, boolean isDelete) {
        return tagImageByImageID(dockerClient, imageId, registry, imageTag, isDelete);
    }



    /**
     * 指定镜像打标签 docker tag imageId helloworld:3.0
     *
     * @param dockerClient DockerClient
     * @param imageId      源镜像id registry:tag方式
     * @param registry     镜像名称
     * @param imageTag     镜像标签
     * @param isDelete     是否删除源镜像标签
     * @return true 成功，false 失败
     */
    public static boolean tagImageByImageID(DockerClient dockerClient, String imageId, String registry, String imageTag, boolean isDelete) {
        try {
            LOG.info("the image imageid is {}, the new imageID is {}", imageId, registry + ":" + imageTag);
            dockerClient.tagImageCmd(imageId, registry, imageTag).exec();
            if (isDelete && !imageId.equals(registry + ":" + imageTag)) {
                deleteImageFromLocal(dockerClient, imageId);
            }
        } catch (Exception e) {
            LOG.error("tag failed {}", e);
        }
        return existImage(dockerClient, registry + ":" + imageTag);
    }

    /**
     * 判断镜像是否存在
     *
     * @param imageId 镜像id：e4f26b47f651或者 image:tag方式
     * @return true存在;false不存在
     */
    public static boolean existImage(String imageId) {
        return existImage(dockerClient, imageId);
    }

    /**
     * 判断镜像是否存在
     */
    public static boolean existImage(DockerClient dockerClient, String imageId) {
        try {
            dockerClient.inspectImageCmd(imageId).exec();
            LOG.info("the image {} is exist ", imageId);
            return true;
        } catch (Exception e) {
            LOG.error("the image {} is NotFound , No such image. {}", imageId, e.getMessage());
            return false;
        }
    }


    /**
     *
     * @param registry
     * @param imageName
     * @param imageTag
     * @return
     */
    public static IpsResult pullImage(Registry registry, String imageName, String imageTag) {
        return pullImage(dockerClient, registry, imageName, imageTag);
    }

    public static IpsResult pullImage(DockerClient dockerClient,String srcImageUri) {
        // TODO 还没测试
        return pullImage(dockerClient, null, srcImageUri, null);
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
        String registryImageURI = registry != null ?  registry.getRegistryImageURI(imageName) : imageName;
        AuthConfig authConfig = registry != null ?  registry.getAuthConfig() : new AuthConfig();
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
        String repository = registry.getImagePushUri(imageName, imageTag); //仓库名称192.168.243.2:5000/helloword
        LOG.info("###############start push the image {} ###############", repository);
        IpsResult result = new IpsResult();
        if (authConfig == null) {
            authConfig = new AuthConfig();
        }
        PushImageResultCallback hpsRes = dockerClient.pushImageCmd(repository).withTag(imageTag)
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
                deleteImageFromLocal(dockerClient, repository);
            }
        }
        LOG.info("###############push image success ###############");
        result.setResult(true);
        return result;
    }

    public boolean pushImage(DockerClient dockerClient, String srcImageId, Registry dstRegistry, String dstImage, String dstTsgId) {
        try{
            // TODO
            boolean result = false;
            LOG.info("Step1: Tag the image");
            pullImage(dockerClient,srcImageId);
            String imageUrl = dstRegistry.getRegistryImageURI(dstImage);
            result = tagImageByImageID(dockerClient,srcImageId,imageUrl,dstTsgId,false);
            IpsResult rs =  pushImage(dockerClient,dstRegistry,dstImage,dstTsgId);
        }catch (Exception e)
        {

        }
        return false;
    }

    /**
     * 查询指定主机本地所有Image信息
     *
     * @return List<Image>
     */
    public static List<Image> listImageInfoFromLocal(DockerClient dockerClient) {
        try {
            List<Image> imageList = dockerClient.listImagesCmd().exec();
            for (Image image : imageList) {
                LOG.info("Image Info : id = {} repotags = {} ", image.getId(), image.getRepoTags());
            }
            return imageList;
        } catch (Exception e) {
            LOG.error("list image info from local error {}", e.getMessage());
            return new ArrayList<Image>();
        }
    }

    /**
     * 查询本地所有Image信息
     *
     * @return List<Image>
     */
    public static List<Image> listImageInfoFromLocal() {
        return listImageInfoFromLocal(dockerClient);
    }

    /**
     * 查询指定本地镜像信息
     *
     * @return List<Image>
     */
    public static List<Image> listImageInfoFromLocal(DockerClient dockerClient, String imageId) {
        List<Image> result = new ArrayList<Image>();
        try {
            List<Image> imageList = dockerClient.listImagesCmd().exec();
            for (Image image : imageList) {
                String sub = imageId.substring(0, 12);
                // sha256:182810e6ba8c469a88d639695b38cfb213aa0120f673a5f55c60ae052b395246
                String imageIdSub = image.getId().substring(7, 19);// 开头为sha256:
                LOG.debug("listImageInfoFromLocal:imageId: {} <-->  list imageId{}", sub, imageIdSub);
                if (image.getId().equals(imageId) || (sub.equals(imageIdSub))) {
                    result.add(image);
                }
            }
        } catch (Exception e) {
            LOG.error("listImageInfoFromLocal {}", e.getMessage());
        }
        return result;
    }

    /**
     * 删除本地某个镜像 若存在容器依赖，则不删除
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
            LOG.info("Delete the local image {} success. ", imageId);
            return true;
        } catch (Exception e) {
            LOG.error("Delete local image {} failed {}", imageId, e.getMessage());
            return false;
        }
    }
}
