/*
 *  Copyright 2010 Vodafone Group Services Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *    
 */
package org.onesocialweb.smack.packet.pubsub;

import org.jivesoftware.smack.packet.IQ;

public class IQPubSubPublish extends IQ {	
	
	private final String entry;
	
	private final String node;
	
	public IQPubSubPublish(String node, String entry) {
		this.entry = entry;
		this.node  = node;
	}
	
	@Override
	public String getChildElementXML() {
		StringBuffer buffer = new StringBuffer();
        buffer.append("<pubsub xmlns='http://jabber.org/protocol/pubsub'>");
        buffer.append("<publish node='" + node + "'>");
        buffer.append("<item>");
        buffer.append(entry);
        buffer.append("</item>");
        buffer.append("</publish>");
        buffer.append("</pubsub>");
		return buffer.toString();
	}
}
