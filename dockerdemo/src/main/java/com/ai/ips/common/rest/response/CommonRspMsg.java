package com.ai.ips.common.rest.response;

import org.apache.http.Header;

/**
 * Created by liuwj on 2015/10/23.
 * 通用响应消息
 */
public class CommonRspMsg {
    // 结果码
    private int resultCode;
    // 结果描述
    private String result;
    
    private Header[] headers;

    public CommonRspMsg(int resultCode, String result) {
        this.resultCode = resultCode;
        this.result = result;
    }

    public CommonRspMsg(int resultCode, String result, Header[] headers) {
        this.resultCode = resultCode;
        this.result = result;
        this.headers = headers;
    }
    
    public CommonRspMsg() {
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
    
    public Header[] getHeaders() {
		return headers;
	}

	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}

	@Override
    public String toString() {
        return "com.ai.ips.common.rest.response.CommonRspMsg{" +
                "resultCode=" + resultCode +
                ", result='" + result + '\'' +
                '}';
    }
}
