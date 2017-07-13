package com.ai.ips.common.docker;

/**
 * Created by liuwj on 2015/10/15.
 * 镜像信息
 */
public class DockerImage extends BaseImage {


    public DockerImage(BaseRegistry registry, String imageName, String tag) {
        super(registry, imageName, tag);
    }

    public DockerImage(BaseRegistry registry, String imageName, String tag, String imageId) {
        super(registry, imageName, tag, imageId);
    }


    public DockerImage(BaseRegistry registry, String imageName, String tag, String imageId, long size) {
        super(registry, imageName, tag, imageId, size, "");
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
     *
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
        return "DockerImage{" +
                "registry=" + registry.toString() +
                ", imageName='" + imageName + '\'' +
                ", imageId='" + imageId + '\'' +
                ", tag='" + tag + '\'' +
                ", size=" + size +
                ", desc='" + desc + '\'' +
                '}';
    }
}
