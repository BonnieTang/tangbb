package com.ai.ips.common.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Desc:镜像基类
 * User: TangBingbing
 * NT: tangbb/70288
 * Date：2017/7/13
 * Version: 1.0
 * Created by IntelliJ IDEA.
 * To change this template use File | Settings | File and Code Templates.
 */
public abstract class BaseImage implements IpsImage {
    protected static final Logger LOG = LoggerFactory.getLogger(BaseImage.class);

    /**
     * 镜像所属仓库
     */
    protected BaseRegistry registry;
    protected String imageName;
    protected String tag;
    protected String imageId;
    protected long size;
    protected String desc;

    public BaseImage(BaseRegistry registry, String imageName, String tag) {
        this.registry = registry;
        this.imageName = imageName;
        this.tag = tag;
    }

    public BaseImage(BaseRegistry registry, String imageName, String tag, String imageId) {
        this.registry = registry;
        this.imageName = imageName;
        this.tag = tag;
        this.imageId = imageId;
    }

    public BaseImage(BaseRegistry registry, String imageName, String tag, String imageId, long size, String desc) {
        this.registry = registry;
        this.imageName = imageName;
        this.tag = tag;
        this.imageId = imageId;
        this.size = size;
        this.desc = desc;
    }

    public BaseRegistry getRegistry() {
        return registry;
    }


    public void setRegistry(BaseRegistry registry) {
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

}
