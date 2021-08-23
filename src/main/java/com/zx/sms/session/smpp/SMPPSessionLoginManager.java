package com.zx.sms.session.smpp;

import com.zx.sms.codec.smpp.SmppConstants;
import com.zx.sms.codec.smpp.Tlv;
import com.zx.sms.codec.smpp.TlvConvertException;
import com.zx.sms.codec.smpp.msg.*;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.smpp.SMPPEndpointEntity;
import com.zx.sms.connect.manager.smpp.SMPPServerEndpointEntity;
import com.zx.sms.session.AbstractSessionLoginManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMPPSessionLoginManager extends AbstractSessionLoginManager {
	private static final Logger logger = LoggerFactory.getLogger(SMPPSessionLoginManager.class);
	public SMPPSessionLoginManager(EndpointEntity entity) {
		super(entity);
	}

	@Override
	protected void doLogin(Channel ch) {
		//发送bind请求
		SMPPEndpointEntity smppentity = (SMPPEndpointEntity) entity;
		BaseBind bind = createBindRequest(smppentity);
		ch.writeAndFlush(bind);
	}

	@Override
	protected EndpointEntity queryEndpointEntityByMsg(Object msg) {
		
		if(msg instanceof BaseBind){
			BaseBind  message = (BaseBind)msg;
			String username = message.getSystemId();
			if (entity instanceof SMPPServerEndpointEntity) {
				SMPPServerEndpointEntity serverEntity = (SMPPServerEndpointEntity) entity;
				if( msg instanceof BindTransmitter){
					EndpointEntity end =  serverEntity.getChild(username.trim(),ChannelType.DOWN);
					return end;
				}else if(msg instanceof BindReceiver){
					EndpointEntity end =  serverEntity.getChild(username.trim(),ChannelType.UP);
					return end;
				}else if(msg instanceof BindTransceiver){
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

	@Override
	protected int validClientMsg(EndpointEntity entity, Object msg) {
		SMPPEndpointEntity smppentity = (SMPPEndpointEntity) entity;
		BaseBind  message = (BaseBind)msg;
		if(smppentity.getSystemId().equals(message.getSystemId()) && 
				smppentity.getPassword().equals(message.getPassword()))
		{
			return 0;
		}else{
			return 3;
		}
		
	}

	@Override
	protected int validServermsg(Object message) {
		if(message instanceof BaseBindResp){
			BaseBindResp resp = (BaseBindResp)message;
			
			Tlv scInterfaceVersion = resp.getOptionalParameter(SmppConstants.TAG_SC_INTERFACE_VERSION);

	            if (scInterfaceVersion != null) {
	                try {
	                    byte tempInterfaceVersion = scInterfaceVersion.getValueAsByte();
	                    logger.info("Server support version : {}" ,tempInterfaceVersion);
	                } catch (TlvConvertException e) {
	                    logger.warn("Unable to convert sc_interface_version to a byte value: {}", e.getMessage());
	                }
	            }
				
			return resp.getCommandStatus();
		}else{
			logger.error("connect msg type error : {}" , message);
			return 9;
		}

	}

	@Override
	protected void changeProtoVersion(ChannelHandlerContext ctx, EndpointEntity entity, Object message) throws Exception {

		
	}

	@Override
	protected void doLoginSuccess(ChannelHandlerContext ctx, EndpointEntity entity, Object message) {
		//发送bind请求
		SMPPEndpointEntity smppentity = (SMPPEndpointEntity) entity;
		
		BaseBind bind = (BaseBind)message;
		BaseBindResp resp =   (BaseBindResp)bind.createResponse();
		resp.setSystemId(bind.getSystemId());
		ctx.channel().writeAndFlush(resp);
	}

	@Override
	protected void failedLogin(ChannelHandlerContext ctx, Object msg, long status) {
		if(msg instanceof BaseBind){
			logger.error("Connected error status :{},msg : {}" , status,msg);
			BaseBind message = (BaseBind)msg;
			// 认证失败
			PduResponse resp = message.createResponse();
			resp.setCommandStatus((int)SmppConstants.STATUS_BINDFAIL);
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
	
    private BaseBind createBindRequest(SMPPEndpointEntity entity)  {
        BaseBind bind = null;
        if (entity.getChannelType() == ChannelType.DUPLEX) {
            bind = new BindTransceiver();
        } else if (entity.getChannelType() == ChannelType.UP) {
            bind = new BindReceiver();
        } else if (entity.getChannelType() == ChannelType.DOWN) {
            bind = new BindTransmitter();
        } else {
        	logger.error("Unable to convert SmppSessionConfiguration into a BaseBind request");
        }
        bind.setSystemId(entity.getSystemId());
        bind.setPassword(entity.getPassword());
        bind.setSystemType(entity.getSystemType());
        bind.setInterfaceVersion(entity.getInterfaceVersion());
        bind.setAddressRange(entity.getAddressRange());
        return bind;
    }

}
