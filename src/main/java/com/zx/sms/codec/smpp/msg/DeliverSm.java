package com.zx.sms.codec.smpp.msg;

import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.smpp.SmppConstants;

import java.util.ArrayList;
import java.util.List;

public class DeliverSm extends BaseSm<DeliverSmResp> implements LongSMSMessage<DeliverSm> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6858655335844462036L;

	public DeliverSm() {
        super(SmppConstants.CMD_ID_DELIVER_SM, "deliver_sm");
    }

    @Override
    public DeliverSmResp createResponse() {
        DeliverSmResp resp = new DeliverSmResp();
        resp.setSequenceNumber(this.getSequenceNumber());
        return resp;
    }

    @Override
    public Class<DeliverSmResp> getResponseClass() {
        return DeliverSmResp.class;
    }

	@Override
	public LongMessageFrame generateFrame() {
		
		return doGenerateFrame();
	}

	@Override
	public DeliverSm generateMessage(LongMessageFrame frame) {
		try {
			return (DeliverSm)doGenerateMessage(frame);
		} catch (Exception e) {
			return null;
		}
	}
	private List<DeliverSm> fragments = null;
	
	@Override
	public List<DeliverSm> getFragments() {
		return fragments;
	}

	@Override
	public void addFragment(DeliverSm fragment) {
		if(fragments==null)
			fragments = new ArrayList<DeliverSm>();
		
		fragments.add(fragment);
	}
}