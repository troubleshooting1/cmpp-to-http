/**
 * 
 */
package com.zx.sms.codec.sgip12.packet;

import com.zx.sms.codec.cmpp.packet.PacketStructure;
import com.zx.sms.codec.cmpp.packet.PacketType;
import com.zx.sms.codec.sgip12.codec.*;
import io.netty.handler.codec.MessageToMessageCodec;

/**
 * @author huzorro(huzorro@gmail.com)
 *
 */
public enum SgipPacketType implements PacketType {
	BINDREQUEST(0x00000001, SgipBindRequest.class,SgipBindRequestMessageCodec.class),
	BINDRESPONSE(0x80000001, SgipBindResponse.class,SgipBindResponseMessageCodec.class),
	UNBINDREQUEST(0x00000002, SgipUnbindRequest.class,SgipUnbindRequestMessageCodec.class),
	UNBINDRESPONSE(0x80000002, SgipUnbindResponse.class,SgipUnbindResponseMessageCodec.class),
	SUBMITREQUEST(0x00000003, SgipSubmitRequest.class,SgipSubmitRequestMessageCodec.class),
	SUBMITRESPONSE(0x80000003, SgipSubmitResponse.class,SgipSubmitResponseMessageCodec.class),
	DELIVERREQUEST(0x00000004, SgipDeliverRequest.class,SgipDeliverRequestMessageCodec.class),
	DELIVERRESPONSE(0x80000004, SgipDeliverResponse.class,SgipDeliverResponseMessageCodec.class),
	REPORTREQUEST(0x00000005, SgipReportRequest.class,SgipReportRequestMessageCodec.class),
	REPORTRESPONSE(0x80000005, SgipReportResponse.class,SgipReportResponseMessageCodec.class);

    private int commandId;
    private Class<? extends PacketStructure> packetStructure;
    private Class<? extends MessageToMessageCodec> codec;
    private SgipPacketType(int commandId, Class<? extends PacketStructure> packetStructure,Class<? extends MessageToMessageCodec> codec) {
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
        for(SgipPacketType packetType : SgipPacketType.values()) {
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
