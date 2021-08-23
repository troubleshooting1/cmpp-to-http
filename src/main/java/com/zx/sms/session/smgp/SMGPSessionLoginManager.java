package com.zx.sms.session.smgp;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.zx.sms.codec.smgp.codec.SMGPMessageCodec;
import com.zx.sms.codec.smgp.msg.SMGPLoginMessage;
import com.zx.sms.codec.smgp.msg.SMGPLoginRespMessage;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.smgp.SMGPEndpointEntity;
import com.zx.sms.connect.manager.smgp.SMGPServerChildEndpointEntity;
import com.zx.sms.connect.manager.smgp.SMGPServerEndpointEntity;
import com.zx.sms.session.AbstractSessionLoginManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class SMGPSessionLoginManager extends AbstractSessionLoginManager {
	private static final Logger logger = LoggerFactory.getLogger(SMGPSessionLoginManager.class);
	public SMGPSessionLoginManager(EndpointEntity entity) {
		super(entity);
	}

	@Override
	protected void doLogin(Channel ch) {
		//发送bind请求
		SMGPEndpointEntity cliententity = (SMGPEndpointEntity) entity;
		
		// TODO 发送连接请求 ,创建密码
		SMGPLoginMessage req = new SMGPLoginMessage();
		req.setClientId(cliententity.getClientID());
		String timestamp = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "MMddHHmmss");
		req.setTimestamp(Long.parseLong(timestamp));
		byte[] userBytes = cliententity.getClientID().getBytes(cliententity.getChartset());
		byte[] passwdBytes = cliententity.getPassword().getBytes(cliententity.getChartset());
		byte[] timestampBytes = timestamp.getBytes(cliententity.getChartset());
		req.setClientAuth(DigestUtils.md5(Bytes.concat(userBytes, new byte[7], passwdBytes, timestampBytes)));
		req.setVersion(cliententity.getClientVersion());
		byte loginMode = (byte)(cliententity.getChannelType() == ChannelType.DUPLEX ? 2 : (cliententity.getChannelType() == ChannelType.UP ? 1:0));
		req.setLoginMode(loginMode);
		ch.writeAndFlush(req);
		logger.info("session Start :Send SMGPLoginMessage seq :{}", req.getSequenceNo());
	}

	@Override
	protected EndpointEntity queryEndpointEntityByMsg(Object msg) {
		
		if(msg instanceof SMGPLoginMessage){
			SMGPLoginMessage  message = (SMGPLoginMessage)msg;
			String username = message.getClientId();
			byte loginMode = message.getLoginMode();
			if (entity instanceof SMGPServerEndpointEntity) {
				SMGPServerEndpointEntity serverEntity = (SMGPServerEndpointEntity) entity;
				
				if(loginMode == 0){
					EndpointEntity end =  serverEntity.getChild(username.trim(),ChannelType.DOWN);
					return end;
				}else if(loginMode == 1){
					EndpointEntity end =  serverEntity.getChild(username.trim(),ChannelType.UP);
					return end;
				}else if(loginMode == 2){
					EndpointEntity end =  serverEntity.getChild(username.trim(),ChannelType.DUPLEX);
					return end;
				}
			}
		}
		return null;
	}

	@Override
	protected boolean validAddressHost(EndpointEntity childentity,Channel channel) {
		return true;
	}
	
	private int validClientMsg(SMGPLoginMessage message, SMGPServerChildEndpointEntity entity){
		byte[] userBytes = entity.getClientID().getBytes(entity.getChartset());
		byte[] passwdBytes = entity.getPassword().getBytes(entity.getChartset());

		byte[] timestampBytes = String.format("%010d", message.getTimestamp()).getBytes(entity.getChartset());
		byte[] authBytes = DigestUtils.md5(Bytes.concat(userBytes, new byte[7], passwdBytes, timestampBytes));
		if (Arrays.equals(authBytes, message.getClientAuth())) {
			return 0;
		} else {
			logger.error("AuthenticatorSource valided failed");
			return 3;
		}
	}

	@Override
	protected int validClientMsg(EndpointEntity entity, Object msg) {
		SMGPServerChildEndpointEntity smgpentity = (SMGPServerChildEndpointEntity) entity;
		SMGPLoginMessage  message = (SMGPLoginMessage)msg;
		return validClientMsg(message,smgpentity);
	}

	@Override
	protected int validServermsg(Object message) {
		if(message instanceof SMGPLoginRespMessage){
			SMGPLoginRespMessage resp = (SMGPLoginRespMessage) message;
			//不校验服务器验证码了。直接返回状态
			return (int) resp.getStatus();
		}else{
			logger.error("connect msg type error : {}" , message);
			return 9;
		}
	}

	@Override
	protected void changeProtoVersion(ChannelHandlerContext ctx, EndpointEntity entity, Object msg) throws Exception {
		SMGPLoginMessage message = (SMGPLoginMessage)msg;
		final short clientVersion = message.getVersion();
		short aim_ver = clientVersion;
		if(entity != null) {
			SMGPServerChildEndpointEntity childentity = (SMGPServerChildEndpointEntity)entity;
			//以服务端配置的版本为准
			aim_ver = childentity.getClientVersion();
			//发送ConnectRequest里的Version跟配置的不同
			if(aim_ver != clientVersion){
				logger.warn("receive version code {} ,expected version is {} .I would use version {}",clientVersion ,aim_ver,aim_ver);
			}
		}
	
		//默认的是3.0的协议，如果不是则要更换解析器版本
		if ((byte)0x30 != aim_ver) {
			
			//以配置的协议版本为准
			//更换协议解析器
			logger.info("changeCodec to version:{}", aim_ver);
			ctx.pipeline().replace(GlobalConstance.codecName, GlobalConstance.codecName,new SMGPMessageCodec(aim_ver));
		}
	}

	@Override
	protected void doLoginSuccess(ChannelHandlerContext ctx, EndpointEntity entity, Object message) {
		//发送bind请求
		SMGPServerChildEndpointEntity smgpentity = (SMGPServerChildEndpointEntity) entity;
		
		SMGPLoginMessage req = (SMGPLoginMessage)message;
		SMGPLoginRespMessage resp = new SMGPLoginRespMessage();
		resp.setSequenceNo(req.getSequenceNo());
		resp.setStatus(0);
		resp.setVersion(smgpentity.getClientVersion());
		resp.setServerAuth(DigestUtils.md5(Bytes.concat(Ints.toByteArray((int)resp.getStatus()), req.getClientAuth(), smgpentity
				.getPassword().getBytes(smgpentity.getChartset()))));
		ctx.channel().writeAndFlush(resp);

	}

	@Override
	protected void failedLogin(ChannelHandlerContext ctx, Object msg, long status) {
		if(msg instanceof SMGPLoginMessage){
			logger.error("Connected error status :{},msg : {}" , status,msg);
			SMGPLoginMessage message = (SMGPLoginMessage)msg;
			// 认证失败
			SMGPLoginRespMessage resp = new SMGPLoginRespMessage();
			resp.setSequenceNo(message.getSequenceNo());
			resp.setStatus((int)status);
			ChannelFuture promise = ctx.writeAndFlush(resp);

			final ChannelHandlerContext finalctx = ctx;
			promise.addListener(new GenericFutureListener() {

				public void operationComplete(Future future) throws Exception {
					finalctx.close();
				}
			});
		}else{
			logger.error("connect msg type error : {}" , msg);
			ctx.close();
		}

	}

}
