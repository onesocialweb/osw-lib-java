package org.onesocialweb.client;

public class XmppJID {

    public static String toBareJID(String jid) {
	if (jid != null) {
		if (jid.contains("/")) {
			return jid.substring(0, jid.indexOf('/'));
		} else {
			return jid;
		}
	}
	
	return null;
    }
}
