package com.zx.sms.codec.cmpp.wap;

import com.zx.sms.LongSMSMessage;
import org.marre.sms.SmsMessage;

class SmsMessageHolder {
	SmsMessage smsMessage;
	LongSMSMessage msg;
	SmsMessageHolder(SmsMessage smsMessage,LongSMSMessage msg){
		this.msg = msg;
		this.smsMessage = smsMessage;
	}
}
