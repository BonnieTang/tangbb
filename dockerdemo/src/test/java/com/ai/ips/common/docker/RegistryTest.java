package com.ai.ips.common.docker;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * Registry Tester.
 *
 * @author tangbb
 * @version 1.0
 */
public class RegistryTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getRegistryImageURI(String imageName)
     */
    @Test
    public void testGetRegistryImageURI() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getTagsListUri(String imageName)
     */
    @Test
    public void testGetTagsListUri() throws Exception {
        // 无认证
        Registry registry = new Registry("10.1.245.31");
        System.out.println(registry.getTagsListUri("hello-world"));
        // 有认证
        registry = new Registry("10.20.16.214", "auth_user1", "123");
        System.out.println(registry.getTagsListUri("hello-world"));
    }

    /**
     * Method: getImagePushUri(String imageName, String imageTag)
     */
    @Test
    public void testGetImagePushUri() throws Exception {
        // 无认证
        Registry registry = new Registry("10.1.245.31");
        System.out.println(registry.getImagePushUri("hello-world","latest"));
        // 有认证
        registry = new Registry("10.20.16.214", "auth_user1", "123");
        System.out.println(registry.getImagePushUri("hello-world","latest"));
    }

    /**
     * Method: getUri()
     */
    @Test
    public void testGetUri() throws Exception {
        // 无认证
        Registry registry = new Registry("10.1.245.31");
        System.out.println(registry.getUri());
        // 有认证
        registry = new Registry("10.20.16.214", "auth_user1", "123");
        System.out.println(registry.getUri());
    }

    /**
     * Method: checkStatus()
     */
    @Test
    public void testCheckStatus() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: auth()
     */
    @Test
    public void testAuth() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getAuthConfig()
     */
    @Test
    public void testGetAuthConfig() throws Exception {
        // 无认证
        Registry registry = new Registry("10.1.245.31");
        System.out.println(registry.getAuthConfig().toString());
        // 有认证
        registry = new Registry("10.20.16.214", "auth_user1", "123");
        System.out.println(registry.getAuthConfig().toString());
    }

    /**
     * Method: toString()
     */
    @Test
    public void testToString() throws Exception {
//TODO: Test goes here... 
    }


} 
