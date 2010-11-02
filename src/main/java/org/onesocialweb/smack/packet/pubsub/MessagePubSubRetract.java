package org.onesocialweb.smack.packet.pubsub;


public class MessagePubSubRetract extends MessagePubSubEvent {

	private final String node;

	private final String id;

	public String getId() {
		return id;
	}

	public MessagePubSubRetract(String node, String id) {
		this.node = node;
		this.id=id;
	}

	

	@Override
	public String toXML() {
		final StringBuffer buf = new StringBuffer();		
		buf.append("<event xmlns='http://jabber.org/protocol/pubsub#event'>");
		buf.append("<items node='" + node + "' />");
		if (id!=null)
		{
			buf.append("<retract id='");
			buf.append(getId());
			buf.append("' />");
		}
		buf.append("</items>");
		buf.append("</event>");
		return buf.toString();
	}

	
}
