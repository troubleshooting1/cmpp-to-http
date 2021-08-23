package com.sb.cmpptohttp.domain.enums;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 短信错误码枚举
 *
 * @author
 */
public enum SmsStatusCodeEnum {

    /**
     * 成功
     */
    SUCCESS(0, "SUCCESS"),

    /**
     * 通用失败
     */
    UNDELIV(1, "UNDELIV"),
    ;

    private Integer code;
    private String name;

    SmsStatusCodeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static SmsStatusCodeEnum codeOf(Integer code){
        Optional<SmsStatusCodeEnum> optionalSmsErrorCodeEnum = Stream.of(SmsStatusCodeEnum.values())
                .filter(k->k.getCode().equals(code)).findFirst();
        return optionalSmsErrorCodeEnum.orElse(null);
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
