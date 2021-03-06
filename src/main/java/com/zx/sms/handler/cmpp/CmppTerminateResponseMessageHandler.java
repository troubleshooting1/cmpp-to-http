/**
 * 
 */
package com.zx.sms.handler.cmpp;

import com.zx.sms.codec.cmpp.msg.CmppTerminateResponseMessage;
import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.codec.cmpp.packet.PacketType;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
@Sharable
public class CmppTerminateResponseMessageHandler extends SimpleChannelInboundHandler<CmppTerminateResponseMessage> {
	private PacketType packetType;

	/**
	 * 
	 */
	public CmppTerminateResponseMessageHandler() {
		this(CmppPacketType.CMPPTERMINATERESPONSE);
	}

	public CmppTerminateResponseMessageHandler(PacketType packetType) {
		this.packetType = packetType;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, CmppTerminateResponseMessage e) throws Exception {
		ctx.channel().close();
	}

}
