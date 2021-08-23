package com.sb.cmpptohttp.controller;


import com.sb.cmpptohttp.domain.form.SendMsgForm;
import com.sb.cmpptohttp.domain.result.Result;
import com.sb.cmpptohttp.service.SmsService;
import com.sb.cmpptohttp.util.IdGenerateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信相关控制器，例如，可以选择某个通道发送短信信息
 *
 * @author chenqiang
 */
@RequestMapping("/sms")
@RestController
public class SmsController {

    @Autowired
    private SmsService msgService;

    @Autowired
    private IdGenerateUtil idGenerateUtil;

    /**
     * 发送短信消息
     *
     * @param sendMsgForm
     */
    @PostMapping("/sendMsg")
    public Result sendMsg(@RequestBody SendMsgForm sendMsgForm) throws Exception {

        Long mid = idGenerateUtil.getUniqueId();

        // 通道号
        String channelNo = sendMsgForm.getChannelNo();

        // 手机号
        String mobile = sendMsgForm.getMobile();

        // 短信内容
        String content = sendMsgForm.getContent();

        // 用户自扩展
        String extend = sendMsgForm.getExtend();

        Result result = msgService.sendMsg(mid, channelNo, mobile, content, extend);
        return result;
    }
}
