package com.zx.sms.codec.smpp.msg;

import com.zx.sms.LongSMSMessage;
import com.zx.sms.codec.cmpp.wap.LongMessageFrame;
import com.zx.sms.codec.smpp.SmppConstants;

import java.util.ArrayList;
import java.util.List;

public class SubmitSm extends BaseSm<SubmitSmResp>  implements LongSMSMessage<SubmitSm> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4398064962035428672L;
	public SubmitSm() {
        super(SmppConstants.CMD_ID_SUBMIT_SM, "submit_sm");
    }

    @Override
    public SubmitSmResp createResponse() {
        SubmitSmResp resp = new SubmitSmResp();
        resp.setSequenceNumber(this.getSequenceNumber());
        return resp;
    }

    @Override
    public Class<SubmitSmResp> getResponseClass() {
        return SubmitSmResp.class;
    }

	@Override
	public LongMessageFrame generateFrame() {
		
		return doGenerateFrame();
	}
	@Override
	public SubmitSm generateMessage(LongMessageFrame frame) {
		try {
			return (SubmitSm)doGenerateMessage(frame);
		} catch (Exception e) {
			return null;
		}
	}
	private List<SubmitSm> fragments = null;
	
	@Override
	public List<SubmitSm> getFragments() {
		return fragments;
	}

	@Override
	public void addFragment(SubmitSm fragment) {
		if(fragments==null)
			fragments = new ArrayList<SubmitSm>();
		
		fragments.add(fragment);
	}
}