package com.zx.sms.handler.smpp;

import com.zx.sms.codec.smpp.msg.UnbindResp;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
public class UnbindRespMessageHandler extends SimpleChannelInboundHandler<UnbindResp>{

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, UnbindResp msg) throws Exception {
		ctx.channel().close();
	}}