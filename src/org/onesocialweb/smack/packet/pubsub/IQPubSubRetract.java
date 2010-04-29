package org.onesocialweb.smack.packet.pubsub;

import org.jivesoftware.smack.packet.IQ;

public class IQPubSubRetract extends IQ {
	
	private final String node;
	
	private final String entryId;
	
	public IQPubSubRetract(String node, String entryId) {
		this.entryId = entryId;
		this.node  = node;
	}
	
	@Override
	public String getChildElementXML() {
		StringBuffer buffer = new StringBuffer();
        buffer.append("<pubsub xmlns='http://jabber.org/protocol/pubsub'>");
        buffer.append("<retract node='" + node + "'>");
        buffer.append("<item id='");
        buffer.append(entryId);
        buffer.append("' />");
        buffer.append("</retract>");
        buffer.append("</pubsub>");
		return buffer.toString();
	}

}
