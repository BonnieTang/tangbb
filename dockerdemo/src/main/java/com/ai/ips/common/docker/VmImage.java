package com.ai.ips.common.docker;

/**
 * Desc: TODO 虚拟机镜像
 * User: TangBingbing
 * NT: tangbb/70288
 * Date：2017/7/13
 * Version: 1.0
 * Created by IntelliJ IDEA.
 * To change this template use File | Settings | File and Code Templates.
 */
public class VmImage extends BaseImage {


    public VmImage(BaseRegistry registry, String imageName, String tag) {
        super(registry, imageName, tag);
    }

    public VmImage(BaseRegistry registry, String imageName, String tag, String imageId) {
        super(registry, imageName, tag, imageId);
    }

    public VmImage(BaseRegistry registry, String imageName, String tag, String imageId, long size, String desc) {
        super(registry, imageName, tag, imageId, size, desc);
    }

    public String getImageUri() {
        return null;
    }

    public String getManifestsUri() {
        return null;
    }

    public String getImageTagUri() {
        return null;
    }
}
