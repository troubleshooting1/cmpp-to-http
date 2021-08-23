package com.sb.cmpptohttp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 短信通道
 * @author qiangchen
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sms_channel")
public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 通道号
     */
    private String channelNo;

    /**
     * 通道名
     */
    private String channelName;

    /**
     * 运营商类型，0：移动，1：联通，2：电信，3：三网，4：国际
     */
    private Integer operatorType;

    /**
     * 协议类型，0：移动cmpp 2.0，1：移动cmpp 3.0，2：sgip联通，3：电信smgp，4：国际smpp
     */
    private Integer protocol;

    /**
     * 通道类型，0：国内通道，1：国际通道
     */
    private Integer channelType;

    /**
     * 通道ip
     */
    private String channelIp;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 登录用户名
     */
    private String loginName;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 接入号，一般106***
     */
    private String srcId;

    /**
     * 企业代码，对应submit的msg_src
     */
    private String msgSrc;

    /**
     * 单连接速度
     */
    private Integer speed;

    /**
     * 是否启用，0：禁用，1：启用
     */
    private Integer enabled;


    /**
     * 最大连接数
     */
    private Integer maxConnect;

    /**
     * 备注
     */
    private String remark;

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
