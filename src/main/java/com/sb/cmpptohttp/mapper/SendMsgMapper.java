package com.sb.cmpptohttp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sb.cmpptohttp.entity.SendMsg;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 短信发送记录
 *
 * @author chenqiang on 2021/8/18 11:24 上午
 */
public interface SendMsgMapper extends BaseMapper<SendMsg> {

    /**
     * 更新短信记录状态
     *
     * @param mid
     * @param status
     * @param statusCode
     * @return
     */
    @Update("<script> " +
            "update `sms_send_msg` set status = #{status}, status_code=#{statusCode}, gmt_modified=CURRENT_TIMESTAMP  " +
            " where mid = #{mid} " +
            " </script>")
   int updateMsgStatus(@Param("mid") Long mid, @Param("status") Integer status, @Param("statusCode") String statusCode);
}
