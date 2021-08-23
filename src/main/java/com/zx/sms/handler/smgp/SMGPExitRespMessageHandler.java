package com.zx.sms.handler.smgp;

import com.zx.sms.codec.smgp.msg.SMGPExitRespMessage;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
public class SMGPExitRespMessageHandler extends SimpleChannelInboundHandler<SMGPExitRespMessage>{

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SMGPExitRespMessage msg) throws Exception {
		ctx.channel().close();
	}}