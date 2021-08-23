package com.sb.cmpptohttp.domain.enums;

/**
 * 短信协议类型
 *
 * @author chenqiang on
 */
public enum ProtocolTypeEnum {
    CMPP(-1, "CMPP"),

    CMPP20(0, "CMPP 2.0"),

    CMPP30(1, "CMPP 3.0"),

    SGIP(2, "SGIP"),

    /**
     * SMGP 3.x
     */
    SMGP(3, "SMGP"),

    /**
     * SMPP 3.4
     */
    SMPP(4, "SMPP");

    public Integer code;

    public String name;

    ProtocolTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getProtocolType(Integer code) {
        for (ProtocolTypeEnum item : ProtocolTypeEnum.values()) {
            if (item.code.equals(code)) {
                return item.name;
            }
        }
        return null;
    }

    public static ProtocolTypeEnum getByCode(Integer code){
        ProtocolTypeEnum[] values = ProtocolTypeEnum.values();
        for (ProtocolTypeEnum typeEnum : values) {
            if (typeEnum.code.equals(code)){
                return typeEnum;
            }
        }
        return null;
    }
}
