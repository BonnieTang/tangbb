package com.ai.ips.common.docker;

import com.ai.ips.common.msg.IpsResult;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * DockerRegistryClient Tester.
 *
 * @author tangbb
 * @version 1.0
 */
public class DockerRegistryClientTest {


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
        Registry registry = new Registry("10.20.16.214", "auth_user1", "123");
        boolean result = DockerRegistryClient.checkRegistry(registry);
        System.out.println(registry.getUri() + " = " + result);

        // 测试无鉴权仓库
        registry = new Registry("10.1.245.236");
        result = DockerRegistryClient.checkRegistry(registry);
        System.out.println(registry.getUri() + " = " + result);
    }

    @Test
    public void testGetImageInfo() {
        // 测试有认证的仓库
        Registry registry = new Registry("10.1.245.236", 5002, "user1", "123");
        ImageInfo info = DockerRegistryClient.getImageInfo(registry, "hello-world", "latest");
        System.out.println(info.toString());
        // ImageInfo{imageName='tangbb', imageId='f0382882c2d27cf8b4a42f3251d1ac75e913ed4aadcacbcbbb56049db62ba62e', tag='1.0'}

        // 测试有认证的仓库，但不传入认证文件，期待报错
        info = DockerRegistryClient.getImageInfo(registry, "hello-world", "latest", null);
        System.out.println(info.toString());
        // ImageInfo{imageName='tangbb', imageId='null', tag='1.0'}


        // 测试无认证仓库
        registry = new Registry("10.1.245.31");
        info = DockerRegistryClient.getImageInfo(registry, "tangbb", "1.0");
        System.out.println(info.toString());
        // ImageInfo{imageName='tangbb', imageId='64ebc56ed1f7a31eb07452f9c30688aa7ca66ecf15eb5bd1324abd3f7346939d', tag='1.0'}

    }

    @Test
    public void testGetImageTagsList() {
        // 测试有认证的仓库
        Registry registry = new Registry("10.20.16.214", "auth_user1", "123");
        List<ImageInfo> info = DockerRegistryClient.getImageTagsList(registry, "tangbb");
        System.out.println(info.toString());

        registry = new Registry("10.1.245.236");
        info = DockerRegistryClient.getImageTagsList(registry, "tangbb");
        System.out.println(info.toString());
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
        System.out.print(info);
    }


    @Test
    public void testPullImage() {
        DockerClient dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        // 测试pull V2仓库
        Registry registry = new Registry("10.1.245.236", 5002, "user1", "123");
        IpsResult result = DockerRegistryClient.pullImage(dockerClient, registry, "hello-world", "latest");
        System.out.println(result.toString());
        /**
         * Create or modify /etc/docker/daemon.json on the client machine
         *
         *        { "insecure-registries":["myregistry.example.com:5000"] }
         *       Restart docker daemon
         *      sudo /etc/init.d/docker restart
         */
//        // 测试下载V1仓库
//        registry = new Registry("10.1.245.236");
//        IpsResult result = DockerRegistryClient.pullImage(dockerClient, registry, "registry", "2.5.1");
//        System.out.println(result.toString());
//        // start pull the image 10.1.245.31:5000/registry imageTag =2.5.1
//        // IpsResult{result=false, errorMsg='Could not pull image: Digest: sha256:2bdc086dadf298fa91f8bf280aeca81b8f5e5910fa104bdba8b0919366eb4bad'}
//        // TODO 为什么提示下载失败？而查看机器上明明已经下载成功了 可能因为版本问题，V1和v2版本存储格式不一样？
    }

    @Test
    public void testPushImage() {
        DockerClient dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        Registry registry = new Registry("10.1.245.236", 5002, "user1", "123");
        System.out.println(registry.getAuthConfig().toString());
        IpsResult result = DockerRegistryClient.pushImage(dockerClient, registry, "hello-world", "latest");
        System.out.println(result.toString());

        dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        registry = new Registry("10.1.245.236");
        result = DockerRegistryClient.pushImage(dockerClient, registry, "hello-world", "latest");
        System.out.println(result.toString());
    }

    @Test
    public void testListImageInfoFromLocal() {
        DockerClient dockerClient = null;
        List<Image> images = null;
        dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        images = DockerRegistryClient.listImageInfoFromLocal(dockerClient);
        System.out.println(images.toString());

        images = DockerRegistryClient.listImageInfoFromLocal(dockerClient, "1815c82652c0");
        System.out.println(images.toString());
    }


    @Test
    public void testDeleteImageFromLocal() {
        DockerClient dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        String imagesId = "10.1.245.236:5002/hello-world:latest";

        boolean result = DockerRegistryClient.deleteImageFromLocal(dockerClient, imagesId);
        System.out.println("Delete " + imagesId + " " + result);
    }

    @Test
    public void testTagImageByImageID() {
        DockerClient dockerClient = DockerRegistryClient.initSimple("tcp://10.1.245.236:2375");
        String imagesId = "10.1.245.236:5002/hello-world:latest";
        boolean res = DockerRegistryClient.tagImageByImageID(dockerClient, imagesId, "10.1.245.236:5002/hello-world", "3.0", false);
        System.out.println(res);
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
