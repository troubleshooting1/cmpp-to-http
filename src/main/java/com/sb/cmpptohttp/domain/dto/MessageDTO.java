package com.sb.cmpptohttp.domain.dto;

import lombok.Data;

/**
 * @author chenqiang
 */
@Data
public class MessageDTO {

    public MessageDTO(Long mid, String mobile, String extend,
                      String content) {
        this.mid = mid;
        this.mobile = mobile;
        this.extend = extend;
        this.content = content;
    }

    /**
     * 短信id
     */
    private Long mid;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 扩展号
     */
    private String extend;

    /**
     * 短信内容
     */
    private String content;
}
