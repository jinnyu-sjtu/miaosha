package com.miaoshaproject.miaosha.error;

/**
 * @Author yujin
 * @Date 2022/2/19 16:55
 * @Version 1.0
 */
public interface CommonError {
    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);

}
