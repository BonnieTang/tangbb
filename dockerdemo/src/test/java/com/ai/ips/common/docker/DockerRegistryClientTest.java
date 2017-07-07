package com.ai.ips.common.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        Registry registry = new Registry("10.20.16.214", "auth_user1", "123");
        ImageInfo info = DockerRegistryClient.getImageInfo(registry, "tangbb", "1.0");
        System.out.println(info.toString());
        // ImageInfo{imageName='tangbb', imageId='f0382882c2d27cf8b4a42f3251d1ac75e913ed4aadcacbcbbb56049db62ba62e', tag='1.0'}

        // 测试有认证的仓库，但不传入认证文件，期待报错
        info = DockerRegistryClient.getImageInfo(registry, "tangbb", "1.0", null);
        System.out.println(info.toString());
        // ImageInfo{imageName='tangbb', imageId='null', tag='1.0'}
        registry = new Registry("10.1.245.31");

        // 测试无认证仓库
        info = DockerRegistryClient.getImageInfo(registry, "tangbb", "1.0");
        System.out.println(info.toString());
        // ImageInfo{imageName='tangbb', imageId='64ebc56ed1f7a31eb07452f9c30688aa7ca66ecf15eb5bd1324abd3f7346939d', tag='1.0'}

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

    /**
     * Method: main(String[] args)
     */
    @Test
    public void testMain() throws Exception {
//TODO: Test goes here... 
    }


} 
