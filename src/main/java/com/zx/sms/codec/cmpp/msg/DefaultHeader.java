package com.zx.sms.codec.cmpp.msg;



/**
 *
 * @author Lihuanghe(18852780@qq.com)
 */
public class DefaultHeader implements Header {
	private static final long serialVersionUID = -3059342529838994125L;
	private long headLength;
    private long packetLength;
    private long bodyLength;
    private int commandId;
    private int sequenceId;
    private long nodeId;

    @Override
    public void setHeadLength(long length) {
        this.headLength = length;
    }

    @Override
    public long getHeadLength() {
        return headLength;
    }

    @Override
    public void setPacketLength(long length) {
        this.packetLength = length;
    }

    @Override
    public long getPacketLength() {
        return packetLength;
    }

    @Override
    public void setBodyLength(long length) {
        this.bodyLength = length;
    }

    @Override
    public long getBodyLength() {
        return bodyLength;
    }

     @Override
    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

     @Override
    public int getCommandId() {
        return commandId;
    }


    @Override
    public void setSequenceId(int transitionId) {
        this.sequenceId = transitionId;
    }

    @Override
    public int getSequenceId() {
        return sequenceId;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DefaultHeader [commandId=0x");
		builder.append(Integer.toHexString(commandId));
		builder.append(", sequenceId=");
		builder.append(sequenceId);
		builder.append(", nodeId=");
		builder.append(nodeId);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public long getNodeId() {
		return nodeId;
	}

	@Override
	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

}
