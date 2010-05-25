package org.onesocialweb.smack.packet.pubsub;

import org.jivesoftware.smack.packet.PacketExtension;

public abstract class MessagePubSubEvent implements PacketExtension{

	@Override
	public String getNamespace() {
		return "http://jabber.org/protocol/pubsub#event";
	}
	
	@Override
	public String getElementName() {
		return "event";
	}
}
