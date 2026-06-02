package com.abc.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(value = "wechat")
@Component
@Data
public class WeChatProperties {
    private String appid;
    private String secret;
    private String mchid;
    private String mchSerialNo;
    private String privateKeyFilePath;
    private String apiV3Key;
    private String weChatPayCertFilePath;
    private String notifyUrl;
    private String refundNotifyUrl;
}
