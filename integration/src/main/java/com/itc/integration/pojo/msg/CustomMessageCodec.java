package com.itc.integration.pojo.msg;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;


/**
 * 自定义消息体 编码器
 *
 */
public class CustomMessageCodec implements MessageCodec<CustomMessage, CustomMessage> {
    @Override
    public void encodeToWire(Buffer buffer, CustomMessage customMessage) {
        JsonObject jsonToEncode = new JsonObject();
        jsonToEncode.put("code", customMessage.getCode());
        jsonToEncode.put("msg", customMessage.getMsg());
        jsonToEncode.put("data", customMessage.getData());

        String jsonToStr = jsonToEncode.encode();

        int length = jsonToStr.getBytes().length;

        buffer.appendInt(length);
        buffer.appendString(jsonToStr);
    }

    @Override
    public CustomMessage decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);

        // Jump 4 because getInt() == 4 bytes
        String jsonStr = buffer.getString(pos += 4, pos + length);
        JsonObject contentJson = new JsonObject(jsonStr);

        Integer code = contentJson.getInteger("code");
        String msg = contentJson.getString("msg");
        JsonObject data = contentJson.getJsonObject("data");

        return new CustomMessage(code, msg, data);
    }

    @Override
    public CustomMessage transform(CustomMessage customMessage) {
        // 这里可以进行消息体转换
        return customMessage;
    }

    @Override
    public String name() {
        // 编码器唯一标识
        return this.getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
