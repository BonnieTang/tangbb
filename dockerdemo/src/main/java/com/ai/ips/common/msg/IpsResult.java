package com.ai.ips.common.msg;

/**
 * Desc:
 * User: TangBingbing
 * NT: tangbb/70288
 * Date：2017/7/7
 * Version: 1.0
 * Created by IntelliJ IDEA.
 * To change this template use File | Settings | File and Code Templates.
 */
public class IpsResult {

    private boolean result = false;
    private String errorMsg = "";

    public IpsResult(){

    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "IpsResult{" +
                "result=" + result +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
