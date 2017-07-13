package com.ai.ips.common.docker;

import com.ai.ips.common.msg.IpsResult;
import com.github.dockerjava.api.model.Image;

import java.util.List;

/**
 * Desc:
 * User: TangBingbing
 * NT: tangbb/70288
 * Date：2017/7/13
 * Version: 1.0
 * Created by IntelliJ IDEA.
 * To change this template use File | Settings | File and Code Templates.
 */
public interface IpsRegistryClient {
    List<IpsImage> getImageTagsList(IpsRegistry registry, String imageName);
    boolean checkRegistry(BaseRegistry registryInfo);
    BaseImage getImageInfo(BaseImage imageInfo);
    boolean tagImageByImageID(String srcImageId, IpsImage dstImage, boolean isDelete);
    boolean existImage(String imageId);
    IpsResult pullImage(BaseImage srcImageName);
    IpsResult pushImage(BaseImage dstImageInfo, boolean delFlag);
    IpsResult pushImage(BaseImage dstImageInfo);
    IpsResult pushImage(BaseImage srcImageInfo, BaseImage dstImageInfo);
    List<Image> listImageInfoFromLocal();
    List<Image> listImageInfoFromLocal(String imageId);
    boolean deleteImageFromLocal(String imageId);
}
