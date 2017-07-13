package com.ai.ips.common.docker;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Desc: 测试基类
 * User: TangBingbing
 * NT: tangbb/70288
 * Date：2017/7/13
 * Version: 1.0
 * Created by IntelliJ IDEA.
 * To change this template use File | Settings | File and Code Templates.
 */
public abstract class BaseTest {
    protected static final Logger LOG = LoggerFactory.getLogger(BaseTest.class);

    @Rule
    public TestName name = new TestName();

    @Before
    public void init() {
        LOG.info("===Start " + name.getMethodName());
    }

    @After
    public void tearDown() throws Exception {
        LOG.info("===End " + name.getMethodName());
    }
}