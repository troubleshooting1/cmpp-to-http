/**
 * 
 */
package com.zx.sms.codec.cmpp.msg;

import com.zx.sms.codec.cmpp.packet.CmppPacketType;
import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.CachedMillisecondClock;

/**
 * @author huzorro(huzorro@gmail.com)
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class CmppQueryResponseMessage extends DefaultMessage {
	private static final long serialVersionUID = 5920218512034934853L;
	private String time = String.format("%tY%<tm%<td", CachedMillisecondClock.INS.now());
	private short queryType = 0;
	private String queryCode = GlobalConstance.emptyString;
	private long mtTLMsg = 0;
	private long mtTLUsr = 0;
	private long mtScs = 0;
	private long mtWT = 0;
	private long mtFL = 0;
	private long moScs = 0;
	private long moWT = 0;
	private long moFL = 0;

	public CmppQueryResponseMessage(int sequenceId) {
		super(CmppPacketType.CMPPQUERYRESPONSE, sequenceId);
	}

	public CmppQueryResponseMessage(Header header) {
		super(CmppPacketType.CMPPQUERYRESPONSE, header);
	}

	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return the queryType
	 */
	public short getQueryType() {
		return queryType;
	}

	/**
	 * @param queryType
	 *            the queryType to set
	 */
	public void setQueryType(short queryType) {
		this.queryType = queryType;
	}

	/**
	 * @return the queryCode
	 */
	public String getQueryCode() {
		return queryCode;
	}

	/**
	 * @param queryCode
	 *            the queryCode to set
	 */
	public void setQueryCode(String queryCode) {
		this.queryCode = queryCode;
	}

	/**
	 * @return the mtTLMsg
	 */
	public long getMtTLMsg() {
		return mtTLMsg;
	}

	/**
	 * @param mtTLMsg
	 *            the mtTLMsg to set
	 */
	public void setMtTLMsg(long mtTLMsg) {
		this.mtTLMsg = mtTLMsg;
	}

	/**
	 * @return the mtTLUsr
	 */
	public long getMtTLUsr() {
		return mtTLUsr;
	}

	/**
	 * @param mtTLUsr
	 *            the mtTLUsr to set
	 */
	public void setMtTLUsr(long mtTLUsr) {
		this.mtTLUsr = mtTLUsr;
	}

	/**
	 * @return the mtScs
	 */
	public long getMtScs() {
		return mtScs;
	}

	/**
	 * @param mtScs
	 *            the mtScs to set
	 */
	public void setMtScs(long mtScs) {
		this.mtScs = mtScs;
	}

	/**
	 * @return the mtWT
	 */
	public long getMtWT() {
		return mtWT;
	}

	/**
	 * @param mtWT
	 *            the mtWT to set
	 */
	public void setMtWT(long mtWT) {
		this.mtWT = mtWT;
	}

	/**
	 * @return the mtFL
	 */
	public long getMtFL() {
		return mtFL;
	}

	/**
	 * @param mtFL
	 *            the mtFL to set
	 */
	public void setMtFL(long mtFL) {
		this.mtFL = mtFL;
	}

	/**
	 * @return the moScs
	 */
	public long getMoScs() {
		return moScs;
	}

	/**
	 * @param moScs
	 *            the moScs to set
	 */
	public void setMoScs(long moScs) {
		this.moScs = moScs;
	}

	/**
	 * @return the moWT
	 */
	public long getMoWT() {
		return moWT;
	}

	/**
	 * @param moWT
	 *            the moWT to set
	 */
	public void setMoWT(long moWT) {
		this.moWT = moWT;
	}

	/**
	 * @return the moFL
	 */
	public long getMoFL() {
		return moFL;
	}

	/**
	 * @param moFL
	 *            the moFL to set
	 */
	public void setMoFL(long moFL) {
		this.moFL = moFL;
	}

	/*
	 * ????????? ????????? ?????? ?????? Time 8 Octet String ??????(????????????)??? Query_Type 1 Unsigned Integer
	 * ??????????????? 0?????????????????? 1??????????????????????????? Query_Code 10 Octet String ???????????? MT_TLMsg 4
	 * Unsigned Integer ???SP????????????????????? MT_Tlusr 4 Unsigned Integer ???SP????????????????????? MT_Scs
	 * 4 Unsigned Integer ????????????????????? MT_WT 4 Unsigned Integer ?????????????????? MT_FL 4
	 * Unsigned Integer ????????????????????? MO_Scs 4 Unsigned Integer ???SP????????????????????? MO_WT 4
	 * Unsigned Integer ???SP?????????????????? MO_FL 4 Unsigned Integer ???SP?????????????????????
	 */

}
