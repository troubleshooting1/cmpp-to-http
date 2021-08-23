package com.sb.cmpptohttp.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 状态报告信息
 *
 * @author qiangchen
 */
@Getter
@Setter
public class ChannelReportMessage {

    public ChannelReportMessage(String msgId, Long appId, Integer feeCount, Long mid, String statusCode, Integer status,
                                String extend, String channelNo, Long logTime, String mobile, Long submitTime,
                                Long reportTm, Boolean isForeignNumber) {
        this.msgId = msgId;
        this.appId = appId;
        this.feeCount = feeCount;
        this.mid = mid;
        this.statusCode = statusCode;
        this.status = status;
        this.extend = extend;
        this.channelNo = channelNo;
        this.logTime = logTime;
        this.mobile = mobile;
        this.submitTime = submitTime;
        this.reportTm = reportTm;
        this.isForeignNumber = isForeignNumber;
    }

    /**
     * 运营商返回短信id
     */
    private String msgId;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 短信计费条数
     */
    private Integer feeCount;

    /**
     * 我们平台自己产生的短信id（处理在我们平台）
     */
    private Long mid;

    /**
     * 状态码
     */
    private String statusCode;

    /**
     * 短信发送状态，0：成功，1：失败，2：未知
     */
    private Integer status;

    /**
     * 扩展号
     */
    private String extend;

    /**
     * 通道号
     */
    private String channelNo;

    /**
     * 日志时间
     */
    private Long logTime;

    /**
     * 发送手机号
     */
    private String mobile;

    /**
     * 提交时间
     */
    private Long submitTime;

    /**
     * 状态报告时间
     */
    private Long reportTm;

    /**
     * 是否是国外号码
     */
    private Boolean isForeignNumber;


}
