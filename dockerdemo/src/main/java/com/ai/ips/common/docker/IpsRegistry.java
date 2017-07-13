package com.ai.ips.common.docker;

import com.github.dockerjava.api.model.AuthConfig;

/**
 * Desc:
 * User: TangBingbing
 * NT: tangbb/70288
 * Date：2017/7/13
 * Version: 1.0
 * Created by IntelliJ IDEA.
 * To change this template use File | Settings | File and Code Templates.
 */
public interface IpsRegistry {
    boolean checkStatus();
    String getSimpleUri();
    String getUri();
    String getTagsListUri(String imageName);
    AuthConfig getAuthConfig();// Ips
}
