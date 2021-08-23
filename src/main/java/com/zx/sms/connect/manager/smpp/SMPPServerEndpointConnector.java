package com.zx.sms.connect.manager.smpp;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.connect.manager.AbstractServerEndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.session.smpp.SMPPSessionLoginManager;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class SMPPServerEndpointConnector extends AbstractServerEndpointConnector{

	public SMPPServerEndpointConnector(EndpointEntity e) {
		super(e);
	}

	@Override
	protected void doinitPipeLine(ChannelPipeline pipeline) {
		super.doinitPipeLine(pipeline);
		EndpointEntity entity = getEndpointEntity();
		pipeline.addLast(GlobalConstance.IdleCheckerHandlerName, new IdleStateHandler(0, 0, entity.getIdleTimeSec(), TimeUnit.SECONDS));
		pipeline.addLast("SmppServerIdleStateHandler", GlobalConstance.smppidleHandler);
		pipeline.addLast(SMPPCodecChannelInitializer.pipeName(), new SMPPCodecChannelInitializer());
		pipeline.addLast("sessionLoginManager", new SMPPSessionLoginManager(getEndpointEntity()));
		
	}
}
