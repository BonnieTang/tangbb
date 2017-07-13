package com.ai.ips.common.docker;

import com.ai.ips.common.msg.IpsResult;
import com.github.dockerjava.api.model.Image;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * DockerRegistryClient Tester.
 *
 * @author tangbb
 * @version 1.0
 */
public class DockerRegistryClientTest extends BaseTest {

    private DockerRegistryClient client = new DockerRegistryClient("tcp://10.1.245.236:2375");

    @Before
    public void before() throws Exception {

    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: checkRegistry(String username, String password, String serverAddress)
     */
    @Test
    public void testCheckRegistry() throws Exception {
        // 测试有鉴权的仓库是否可以连接
        DockerRegistry registry = new DockerRegistry("10.1.245.236", 5002, "user1", "123");
        boolean result = client.checkRegistry(registry);
        LOG.info(registry.getUri() + " = " + result);
        Assert.assertTrue(result);
        // 测试无鉴权仓库
        registry = new DockerRegistry("10.1.245.31");
        result = client.checkRegistry(registry);
        LOG.info(registry.getUri() + " = " + result);
        Assert.assertTrue(result);
        // 测试无鉴权仓库(未启动，访问不成功）
        registry = new DockerRegistry("10.1.245.236");
        result = client.checkRegistry(registry);
        LOG.info(registry.getUri() + " = " + result);
        Assert.assertFalse(result);
    }

    @Test
    public void testGetImageInfo() {
        // 测试有认证的仓库
        DockerRegistry registry = new DockerRegistry("10.1.245.236", 5002, "user1", "123");
        BaseImage info = new DockerImage(registry, "hello-world", "latest");
        LOG.info("===Before " + info.toString());
        info = client.getImageInfo(info);
        LOG.info("===After " + info.toString());
        // DockerImage{imageName='tangbb', imageId='f0382882c2d27cf8b4a42f3251d1ac75e913ed4aadcacbcbbb56049db62ba62e', tag='1.0'}
        int len = info.getImageId() != null ? info.getImageId().length() : 0; // 验证base64的长度
        Assert.assertEquals(64, len);

        // 测试无认证仓库
        registry = new DockerRegistry("10.1.245.31");
        info = new DockerImage(registry, "tangbb", "1.0");
        info = client.getImageInfo(info);
        LOG.info(info.toString());
        len = info.getImageId() != null ? info.getImageId().length() : 0; // 验证base64的长度
        Assert.assertEquals(64, len);
        // DockerImage{imageName='tangbb', imageId='64ebc56ed1f7a31eb07452f9c30688aa7ca66ecf15eb5bd1324abd3f7346939d', tag='1.0'}
    }

    @Test
    public void testGetImageTagsList() {
        // 测试有认证的仓库
        BaseRegistry registry = new DockerRegistry("10.1.245.236", 5002, "user1", "123");
        List<IpsImage> info = client.getImageTagsList(registry, "hello-world");
        LOG.info(info.toString());
        Assert.assertTrue(info.size() > 0);
        registry = new DockerRegistry("10.1.245.31");
        info = client.getImageTagsList(registry, "tangbb");
        LOG.info(info.toString());
        Assert.assertTrue(info.size() > 0);
    }

    @Test
    public void testPullImage() {
        // 测试pull V2仓库
        DockerRegistry registry = new DockerRegistry("10.1.245.236", 5002, "user1", "123");
        DockerImage info = new DockerImage(registry, "hello-world", "latest");
        IpsResult result = client.pullImage(info);
        LOG.info(result.toString());
        Assert.assertTrue(result.isResult());
        /**
         * Create or modify /etc/docker/daemon.json on the client machine
         *
         *        { "insecure-registries":["myregistry.example.com:5000"] }
         *       Restart docker daemon
         *      sudo /etc/init.d/docker restart
         */

    }

    @Test
    public void testPushImage() {

        DockerRegistry registry = new DockerRegistry("10.1.245.236", 5002, "user1", "123");
        BaseImage info = new DockerImage(registry, "hello-world", "latest");
        IpsResult result = client.pushImage(info);
        LOG.info(result.toString());
        Assert.assertTrue(result.isResult());
    }

    @Test
    public void testPull2PushImage() {
        DockerRegistry srcRegistry = new DockerRegistry("10.1.245.31");
        DockerRegistry dstRegistry = new DockerRegistry("10.1.245.236", 5002, "user1", "123");
        DockerImage srcInfo = new DockerImage(srcRegistry, "registry", "2.5.1");
        DockerImage dstInfo = new DockerImage(dstRegistry, "registry", "2.5.2");
        IpsResult result = client.pushImage(srcInfo, dstInfo);
        LOG.info(result.toString());
        Assert.assertTrue(result.isResult());
    }

    @Test
    public void testListImageInfoFromLocal() {
        List<Image> images = null;
        images = client.listImageInfoFromLocal();
        LOG.info(images.toString());
        Assert.assertTrue(images.size() > 0);
        images = client.listImageInfoFromLocal("1815c82652c0");
        LOG.info(images.toString());
        Assert.assertTrue(images.size() > 0);
    }


    @Test
    public void testDeleteImageFromLocal() {
        String imagesId = "10.1.245.236:5002/hello-world:30";

        boolean result = client.deleteImageFromLocal(imagesId);
        LOG.info("Delete " + imagesId + " " + result);
        Assert.assertTrue(result);
    }

    @Test
    public void testTagImageByImageID() {
        String imagesId = "10.1.245.236:5002/hello-world:1.0";
        DockerRegistry dstRegistry = new DockerRegistry("10.1.245.236", 5002, "user1", "123");
        DockerImage dstInfo = new DockerImage(dstRegistry, "hello-world", "30");
        boolean res = client.tagImageByImageID(imagesId, dstInfo, false);
        Assert.assertTrue(res);
    }

    @Test
    public void testExistImage() {

    }

    /**
     * Method: main(String[] args)
     */
    @Test
    public void testMain() throws Exception {
//TODO: Test goes here...
    }


} 
