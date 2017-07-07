package com.ai.ips.common.docker;

/**
 * Created by liuwj on 2015/10/15.
 * 镜像信息
 */
public class ImageInfo {
    private String imageName;
    private String imageId;
    private String tag;
    private long size;
    private String desc;

    public ImageInfo() {}

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

	@Override
    public String toString() {
        return "ImageInfo{" +
                "imageName='" + imageName + '\'' +
                ", imageId='" + imageId + '\'' +
                ", tag='" + tag + '\'' +
                '}';
    }
}
