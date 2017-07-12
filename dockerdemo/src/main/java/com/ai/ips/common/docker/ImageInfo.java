package com.ai.ips.common.docker;

/**
 * Created by liuwj on 2015/10/15.
 * 镜像信息
 */
public class ImageInfo {


    /**
     * 镜像所属仓库
     */
    private Registry registry;
    private String imageName;
    private String imageId;
    private String tag;
    private long size;
    private String desc;

    public ImageInfo() {
    }

    public ImageInfo(Registry registry, String imageName, String tag) {
        this.registry = registry;
        this.imageName = imageName;
        this.tag = tag;
    }

    public ImageInfo(String imageName, String imageId, String tag) {
        this.imageName = imageName;
        this.imageId = imageId;
        this.tag = tag;
    }

    public ImageInfo(String imageName, String imageId, String tag, long size) {
        this.imageName = imageName;
        this.imageId = imageId;
        this.tag = tag;
        this.size = size;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }


    /**
     * 获取本仓库镜像的URL（不含Tag）
     *
     * @return 10.1.234.246:5000/tomcat
     */
    public String getImageUri() {
        return registry.getSimpleUri() + "/" + imageName;
    }


    /**
     * 获取Manifest URI
     * @return http://10.1.245.31:5000/v2/tomcat/manifests/8.0
     */
    public String getManifestsUri() {
        return registry.getUri() + imageName + "/manifests/" + tag;
    }

    /**
     * 获取本仓库镜像的URL(含Tag）
     *
     * @return 10.1.234.246:5000/tomcat:8.0
     */
    public String getImageTagUri() {
        return registry.getSimpleUri() + "/" + imageName + ":" + this.tag;
    }

    @Override
    public String toString() {
        return "ImageInfo{" +
                "imageName='" + imageName + '\'' +
                ", imageId='" + imageId + '\'' +
                ", tag='" + tag + '\'' +
                '}';
    }
}
