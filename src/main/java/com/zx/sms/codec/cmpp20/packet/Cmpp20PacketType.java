/**
 * 
 */
package com.zx.sms.codec.cmpp20.packet;

import com.zx.sms.codec.cmpp.*;
import com.zx.sms.codec.cmpp.packet.*;
import com.zx.sms.codec.cmpp20.*;
import io.netty.handler.codec.MessageToMessageCodec;


/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum Cmpp20PacketType implements PacketType {
    CMPPCONNECTREQUEST(0x00000001, CmppConnectRequest.class,CmppConnectRequestMessageCodec.class),
    CMPPCONNECTRESPONSE(0x80000001, Cmpp20ConnectResponse.class,Cmpp20ConnectResponseMessageCodec.class),
    CMPPTERMINATEREQUEST(0x00000002, CmppTerminateRequest.class,CmppTerminateRequestMessageCodec.class),
    CMPPTERMINATERESPONSE(0x80000002, CmppTerminateResponse.class,CmppTerminateResponseMessageCodec.class),    
    CMPPSUBMITREQUEST(0x00000004, Cmpp20SubmitRequest.class,Cmpp20SubmitRequestMessageCodec.class), 
    CMPPSUBMITRESPONSE(0x80000004, Cmpp20SubmitResponse.class,Cmpp20SubmitResponseMessageCodec.class),
    CMPPDELIVERREQUEST(0x00000005, Cmpp20DeliverRequest.class,Cmpp20DeliverRequestMessageCodec.class),
    CMPPDELIVERRESPONSE(0x80000005, Cmpp20DeliverResponse.class,Cmpp20DeliverResponseMessageCodec.class),    
    CMPPQUERYREQUEST(0x00000006, CmppQueryRequest.class,CmppQueryRequestMessageCodec.class),
    CMPPQUERYRESPONSE(0x80000006, CmppQueryResponse.class,CmppQueryResponseMessageCodec.class),
    CMPPCANCELREQUEST(0x00000007, CmppCancelRequest.class,CmppCancelRequestMessageCodec.class),
    CMPPCANCELRESPONSE(0x80000007, CmppCancelResponse.class,CmppCancelResponseMessageCodec.class),
    CMPPACTIVETESTREQUEST(0x00000008, CmppActiveTestRequest.class,CmppActiveTestRequestMessageCodec.class),
    CMPPACTIVETESTRESPONSE(0x80000008, CmppActiveTestResponse.class,CmppActiveTestResponseMessageCodec.class);
    
    private int commandId;
    private Class<? extends PacketStructure> packetStructure;
    private Class<? extends MessageToMessageCodec> codec;
    
    private Cmpp20PacketType(int commandId, Class<? extends PacketStructure> packetStructure,Class<? extends MessageToMessageCodec> codec) {
        this.commandId = commandId;
        this.packetStructure = packetStructure;
        this.codec = codec;
    }
    public int getCommandId() {
        return commandId;
    }
    public PacketStructure[] getPacketStructures() {
    	return packetStructure.getEnumConstants();
    }

    public long getAllCommandId() {
        long defaultId = 0x0;
        long allCommandId = 0x0;
        for(Cmpp20PacketType packetType : Cmpp20PacketType.values()) {
            allCommandId |= packetType.commandId;
        }
        return allCommandId ^ defaultId;
    }
	@Override
	public MessageToMessageCodec getCodec() {
		
		try {
			return codec.newInstance();
		} catch (InstantiationException e) {
			return null;
		}
		catch(  IllegalAccessException e){
			return null;
		}
	}
}
