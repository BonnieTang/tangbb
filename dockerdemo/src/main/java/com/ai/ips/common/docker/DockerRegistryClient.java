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
public class DockerRegistryClient extends BaseRegistryClient {


    private static final String DefaultDockerServerUrl = "unix:///var/run/docker.sock";

    private DockerClient dockerClient;

    public DockerRegistryClient() {
        this.dockerClient = DockerClientBuilder.getInstance(DefaultDockerServerUrl).build();
    }

    public DockerRegistryClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    /**
     * eg
     *
     * @param serverUrl "tcp://10.1.245.236:2375"
     */
    public DockerRegistryClient(String serverUrl) {
        this.dockerClient = DockerClientBuilder.getInstance(serverUrl).build();
        ;
    }


    /**
     * 判断仓库是否能ping通
     */
    public boolean checkRegistry(BaseRegistry registryInfo) {
        return registryInfo.checkStatus();

    }

    /**
     * 从仓库获取镜像详细信息,填充id
     */
    public BaseImage getImageInfo(BaseImage imageInfo) {
        AuthConfig authConfig = imageInfo.getRegistry().getAuthConfig();
        String uri = imageInfo.getManifestsUri();// http://10.1.245.31:5000/v2/tomcat/manifests/8.0
        CommonRspMsg crm = null;
        try {
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
                LOG.info("Find imageId = {}", imageId);
            }
        } catch (Exception e) {
            LOG.error("Get Info failed .{}", e.getMessage());
        }
        return imageInfo;
    }

    /**
     * 获取单个镜像的所有信息
     */
    public List<IpsImage> getImageTagsList(IpsRegistry registry, String imageName) {
        AuthConfig authConfig = registry.getAuthConfig();
        String uri = registry.getTagsListUri(imageName);
        CommonRspMsg crm = null;
        List<IpsImage> list = new ArrayList<IpsImage>();
        try {
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
                        DockerImage image = new DockerImage((DockerRegistry) registry, imageName, tag);
                        list.add(image);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Get Info failed .{}", e.getMessage());
        }
        return list;
    }

    /**
     * 指定镜像打标签 docker tag imageId helloworld:3.0
     *
     * @param srcImageId 源镜像id registry:tag方式
     * @param dstImage   目标镜像信息
     * @param isDelete   是否删除源镜像标签
     * @return true 成功，false 失败
     */
    public boolean tagImageByImageID(String srcImageId, IpsImage dstImage, boolean isDelete) {
        String dstUri = dstImage.getImageTagUri();
        try {
            LOG.info("the image imageid is {}, the new imageID is {}", srcImageId, dstUri);
            dockerClient.tagImageCmd(srcImageId, dstImage.getImageUri(), dstImage.getTag()).exec();
            if (isDelete && !srcImageId.equals(dstUri)) {
                deleteImageFromLocal(srcImageId);
            }
        } catch (Exception e) {
            LOG.error("tag failed {}", e);
        }
        return existImage(dstUri);
    }

    /**
     * 判断镜像是否存在
     */
    public boolean existImage(String imageId) {
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
     * 下载指定仓库的指定镜像  pull image
     *
     * @param srcImageName 源镜像信息
     */
    public IpsResult pullImage(BaseImage srcImageName) {
        IpsResult result = new IpsResult();
        String registryImageURI = srcImageName.getImageTagUri();
        AuthConfig authConfig = srcImageName.getRegistry().getAuthConfig();
        LOG.info("############### start pull the image {} ###############", registryImageURI);
        PullImageResultCallback res = dockerClient.pullImageCmd(registryImageURI).withTag(srcImageName.getTag())
                .withAuthConfig(authConfig).exec(new PullImageResultCallback());
        try {
            res.awaitSuccess();
        } catch (DockerClientException e) {
            LOG.error("############### pull image failed , {} ###############", e.getMessage());
            // 为什么提示下载失败？而查看机器上明明已经下载成功了 可能因为版本问题，V1和v2版本存储格式不一样？
            if (e.getMessage().startsWith("Could not pull image: Digest: sha256")) //  解决API自身问题
                result.setResult(true);
            else
                result.setResult(false);
            result.setErrorMsg(e.getMessage());
            return result;
        }
        LOG.info("############### pull image success ###############");
        result.setResult(true);
        return result;
    }

    /**
     * 使用本机启动的dorker client，默认上传完成后删除本地镜像
     */
    public IpsResult pushImage(BaseImage dstImageInfo) {
        return pushImage(dstImageInfo, true);
    }

    /**
     * 上传本地镜像到指定私有镜像仓库 push image
     *
     * @param dstImageInfo 目标镜像信息
     * @param delFlag      上传完成后是否删除本地镜像
     */
    public IpsResult pushImage(BaseImage dstImageInfo, boolean delFlag) {
        AuthConfig authConfig = dstImageInfo.getRegistry().getAuthConfig();
        String repository = dstImageInfo.getImageTagUri(); //仓库名称192.168.243.2:5000/helloword:1.0
        String tag = dstImageInfo.getTag();
        LOG.info("###############start push the image {} ###############", repository);
        IpsResult result = new IpsResult();
        if (authConfig == null) {
            authConfig = new AuthConfig();
        }
        try {
            PushImageResultCallback hpsRes = dockerClient.pushImageCmd(repository).withTag(tag)
                    .withAuthConfig(authConfig).exec(new PushImageResultCallback());
            hpsRes.awaitSuccess();
        } catch (DockerClientException e) {
            LOG.error("###############push image failed , {} ##############", e.getMessage());
            result.setResult(false);
            result.setErrorMsg(e.getMessage());
            return result;
        } finally {
            if (delFlag) {
                deleteImageFromLocal(repository);
            }
        }
        LOG.info("###############push image success ###############");
        result.setResult(true);
        return result;
    }


    /**
     * 从源镜像仓库上传到目标镜像仓库
     *
     * @param srcImageInfo 源目标镜像信息
     * @param dstImageInfo 目标目标镜像信息
     * @return 操作结果
     */
    public IpsResult pushImage(BaseImage srcImageInfo, BaseImage dstImageInfo) {
        IpsResult result = new IpsResult();
        try {
            String srcUri = srcImageInfo.getImageTagUri();// 10.1.234.246:5000/tomcat:8.0
            LOG.info("******Step1: Pull the image {}", srcUri);
            IpsResult pullRs = pullImage(srcImageInfo);
            LOG.info("******IpsResult ", pullRs.toString());
            String dstUrl = dstImageInfo.getImageTagUri();
            LOG.info("******Step2: Tag the image {} --> {}", srcUri, dstUrl);
            boolean tag = tagImageByImageID(srcUri, dstImageInfo, false);
            LOG.info("******Tag Result = {} ", result);
            LOG.info("******Step3: Push the image to registry {} , image = {} ", dstImageInfo.getRegistry().toString(), dstImageInfo.getImageTagUri());
            IpsResult pushRs = pushImage(dstImageInfo);
            LOG.info("******IpsResult ", pushRs.toString());
            result.setResult(pullRs.isResult() && tag && pushRs.isResult());
            result.setErrorMsg(pullRs.getErrorMsg() + "" + pushRs.getErrorMsg());
        } catch (Exception e) {
            LOG.error("pushImage failed {}", e.getMessage());
            result.setResult(false);
            result.setErrorMsg(e.getMessage());
        }
        return result;
    }

    /**
     * 查询指定主机本地所有Image信息
     *
     * @return List<Image>
     */
    public List<Image> listImageInfoFromLocal() {
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
     * 查询指定本地镜像信息
     *
     * @return List<Image>
     */
    public List<Image> listImageInfoFromLocal(String imageId) {
        List<Image> result = new ArrayList<Image>();
        try {
            List<Image> imageList = dockerClient.listImagesCmd().exec();
            for (Image image : imageList) {
                String sub = imageId.substring(0, 12);
                // sha256:182810e6ba8c469a88d639695b38cfb213aa0120f673a5f55c60ae052b395246
                String imageIdSub = image.getId().substring(7, 19);// 开头为sha256:
                LOG.debug("listImageInfoFromLocal:imageId: {} <-->  {}", sub, image.getId());
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
     * 如果直接删除存在一个问题，docker是先删除tag 再删除文件：若此imageId只有一个tag，则会导致产生<none>：<none>的镜像
     *
     * @param imageId 镜像imageId  name:tag或id
     * @return false，删除失败；true，删除成功
     */
    public boolean deleteImageFromLocal(String imageId) {
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
