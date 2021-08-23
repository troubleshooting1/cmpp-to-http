package com.sb.cmpptohttp;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.sb.cmpptohttp.domain.form.SendMsgForm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author chenqiang on 2021/8/20 9:39 上午
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SendMsgTest {

    @Test
    public void sendMsg() {
        String url = "http://localhost:8925/sms/sendMsg";

        SendMsgForm sendMsgForm = new SendMsgForm();
        sendMsgForm.setChannelNo("100");
        sendMsgForm.setMobile("19151072436");
        sendMsgForm.setContent("【xx公司】这是一条测试短信");
        sendMsgForm.setExtend("123");

        String result = HttpRequest.post(url)
                .body(JSONUtil.toJsonStr(sendMsgForm))
                .execute().body();

        System.out.println(result);

    }
}
