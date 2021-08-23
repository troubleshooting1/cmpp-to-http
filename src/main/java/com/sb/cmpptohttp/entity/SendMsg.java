package com.sb.cmpptohttp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 短信发送记录
 *
 * @author chenqiang
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sms_send_msg")
public class SendMsg {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 短信唯一id
     */
    private Long mid;

    /**
     * 通道号，选择哪个通道发送短信
     */
    private String channelNo;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 短信内容
     */
    private String content;

    /**
     * 扩展码
     */
    private String extend;

    /**
     * 短信拆分条数
     */
    private Integer count;

    /**
     * 短信状态，0：未知，1：成功，2：失败
     */
    private Integer status;

    /**
     * 错误码
     */
    private String statusCode;

    /**
     * 发送日期，yyyyMMdd
     */
    private Integer sendDate;

    /**
     * 创建时间
     */
    private Date gmtCreated;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * 修改人
     */
    private String modifiedBy;

}