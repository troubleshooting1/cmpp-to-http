package com.zx.sms;

import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import org.marre.sms.SmsMessage;

import java.util.List;

public interface LongSMSMessage<T> {
	public LongMessageFrame generateFrame();
	public T generateMessage(LongMessageFrame frame) throws Exception;
	public SmsMessage getSmsMessage();
	public boolean isReport();
	
	//下面两个方法用来保存合并短信前各个片断对应的消息
	public List<T> getFragments();
	public void addFragment(T fragment);
}
