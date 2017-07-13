package com.ai.ips.common.docker;

import com.ai.ips.common.msg.IpsResult;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;

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
public class DockerRegistryClientTest  extends BaseTest {


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
        Registry registry = new Registry("10.1.245.236", 5002, "user1", "123");
        boolean result = DockerRegistryClient.checkRegistry(registry);
        LOG.info(registry.getUri() + " = " + result);
        Assert.assertTrue(result);
        // 测试无鉴权仓库
        registry = new Registry("10.1.245.31");
        result = DockerRegistryClient.checkRegistry(registry);
        LOG.info(registry.getUri() + " = " + result);
        Assert.assertTrue(result);
        // 测试无鉴权仓库(未启动，访问不成功）
        registry = new Registry("10.1.245.236");
        result = DockerRegistryClient.checkRegistry(registry);
        LOG.info(registry.getUri() + " = " + result);
        Assert.assertFalse(result);
    }

    @Test
    public void testGetImageInfo() {
        // 测试有认证的仓库
        Registry registry = new Registry("10.1.245.236", 5002, "user1", "123");
        ImageInfo info = new ImageInfo(registry, "hello-world", "latest");
        LOG.info("===Before " + info.toString());
        info = DockerRegistryClient.getImageInfo(info);
        LOG.info("===After " + info.toString());
        // ImageInfo{imageName='tangbb', imageId='f0382882c2d27cf8b4a42f3251d1ac75e913ed4aadcacbcbbb56049db62ba62e', tag='1.0'}
        int len = info.getImageId() != null ? info.getImageId().length():0; // 验证base64的长度
        Assert.assertEquals(64,len);

        // 测试无认证仓库
        registry = new Registry("10.1.245.31");
        info = new ImageInfo(registry, "tangbb", "1.0");
        info = DockerRegistryClient.getImageInfo(info);
        LOG.info(info.toString());
        len = info.getImageId() != null ? info.getImageId().length():0; // 验证base64的长度
        Assert.assertEquals(64,len);
        // ImageInfo{imageName='tangbb', imageId='64ebc56ed1f7a31eb07452f9c30688aa7ca66ecf15eb5bd1324abd3f7346939d', tag='1.0'}
    }

    @Test
    public void testGetImageTagsList() {
        // 测试有认证的仓库
        Registry registry = new Registry("10.1.245.236", 5002, "user1", "123");
        List<ImageInfo> info = DockerRegistryClient.getImageTagsList(registry, "hello-world");
        LOG.info(info.toString());
        Assert.assertTrue(info.size() > 0);
        registry = new Registry("10.1.245.31");
        info = DockerRegistryClient.getImageTagsList(registry, "tangbb");
        LOG.info(info.toString());
        Assert.assertTrue(info.size() > 0);
    }

    /**
     * Method: initSimple()
     */
    @Test
    public void testInitSimple() throws Exception {
        // 10.20.16.214
        // 10.1.245.31  version=1.9.1
        // 10.1.245.236
        // 10.1.245.22 version=1.12.6
        DockerClient dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        Info info = dockerClient.infoCmd().exec();
        LOG.info(info.toString());
        Assert.assertTrue(info != null);
    }


    @Test
    public void testPullImage() {
        DockerClient dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        // 测试pull V2仓库
        Registry registry = new Registry("10.1.245.236", 5002, "user1", "123");
        ImageInfo info = new ImageInfo(registry, "hello-world", "latest");
        IpsResult result = DockerRegistryClient.pullImage(dockerClient, info);
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
        DockerClient dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        Registry registry = new Registry("10.1.245.236", 5002, "user1", "123");
        ImageInfo info = new ImageInfo(registry, "hello-world", "latest");
        IpsResult result = DockerRegistryClient.pushImage(dockerClient, info);
        LOG.info(result.toString());
        Assert.assertTrue(result.isResult());
    }

    @Test
    public void testPull2PushImage() {
        DockerClient dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        Registry srcRegistry = new Registry("10.1.245.31");
        Registry dstRegistry = new Registry("10.1.245.236", 5002, "user1", "123");
        ImageInfo srcInfo = new ImageInfo(srcRegistry, "registry", "2.5.1");
        ImageInfo dstInfo = new ImageInfo(dstRegistry, "registry", "2.5.2");
        IpsResult result = DockerRegistryClient.pushImage(dockerClient, srcInfo, dstInfo);
        LOG.info(result.toString());
        Assert.assertTrue(result.isResult());
    }

    @Test
    public void testListImageInfoFromLocal() {
        DockerClient dockerClient = null;
        List<Image> images = null;
        dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        images = DockerRegistryClient.listImageInfoFromLocal(dockerClient);
        LOG.info(images.toString());
        Assert.assertTrue(images.size() > 0);
        images = DockerRegistryClient.listImageInfoFromLocal(dockerClient, "1815c82652c0");
        LOG.info(images.toString());
        Assert.assertTrue(images.size() > 0);
    }


    @Test
    public void testDeleteImageFromLocal() {
        DockerClient dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        String imagesId = "10.1.245.236:5002/hello-world:30";

        boolean result = DockerRegistryClient.deleteImageFromLocal(dockerClient, imagesId);
        LOG.info("Delete " + imagesId + " " + result);
        Assert.assertTrue(result);
    }

    @Test
    public void testTagImageByImageID() {
        DockerClient dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        String imagesId = "10.1.245.236:5002/hello-world:1.0";
        Registry dstRegistry = new Registry("10.1.245.236", 5002, "user1", "123");
        ImageInfo dstInfo = new ImageInfo(dstRegistry, "hello-world", "30");
        boolean res = DockerRegistryClient.tagImageByImageID(dockerClient, imagesId, dstInfo, false);
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
