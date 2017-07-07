package com.ai.ips.common.msg;

/**
 * 消息结果码
 * @author ZHOUYUE
 *
 */
public enum ResultCode {
	// common
	ERC_SUCCESS(0),
	ERC_FAILED(1),
	
	// agent
	ERC_AGENTEXISTS(2),
	
	// observer
	ERC_OBSERVEREXISTS(30),

	// marathon resp
	ERC_MARATHON_STARTAPP_SUCCESS(201),
	
	ERC_SCALE_RESOURCE_INSUFICIENT(301);
	
	private int value;
	
	private ResultCode(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
}
