package com.zx.sms.handler.smpp;

import com.zx.sms.codec.smpp.msg.EnquireLinkResp;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
public class EnquireLinkRespMessageHandler  extends SimpleChannelInboundHandler<EnquireLinkResp>{
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, EnquireLinkResp msg) throws Exception {
		
	}
}


