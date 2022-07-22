package com.itc.integration.pojo.msg;

/**
 * @ClassName CustomMessage 自定义消息体
 * @Author sussenn
 * @Version 1.0.0
 * @Date 2022/7/22
 */
public class CustomMessage {

    private Integer code;
    private String msg;
    private Object data;

    public CustomMessage(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    @Override
    public String toString() {
        return "CustomMessage{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
