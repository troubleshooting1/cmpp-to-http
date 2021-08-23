package com.sb.cmpptohttp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.zx.sms.common.util","com.sb"})
public class CmppToHttpApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmppToHttpApplication.class, args);
    }

}
