package org.onesocialweb.smack.packet.utils;

import org.jivesoftware.smack.packet.IQ;



public class IQPing extends IQ {

	public String getElementName() {
		return "ping";
	}

	public String getNamespace() {
		return "urn:xmpp:ping";
	}	

	@Override
	public String getChildElementXML() {
		return "<ping xmlns='urn:xmpp:ping' />";
	}
}
