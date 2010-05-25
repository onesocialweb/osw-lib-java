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

import java.util.List;

import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.xml.writer.ActivityXmlWriter;

public class MessagePubSubItems extends MessagePubSubEvent {

	private final String node;

	private List<ActivityEntry> entries;

	public MessagePubSubItems(String node) {
		this.node = node;
	}

	public List<ActivityEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<ActivityEntry> entries) {
		this.entries = entries;
	}

	@Override
	public String toXML() {
		final StringBuffer buf = new StringBuffer();
		final ActivityXmlWriter writer = new ActivityXmlWriter();
		buf.append("<event xmlns='http://jabber.org/protocol/pubsub#event'>");
		buf.append("<items node='" + node + "' />");
		if (entries != null) {
			for (ActivityEntry entry : entries) {
				buf.append("<item ");
				if (entry.hasId()) {
					buf.append("id='" + entry.getId() + "'");
				}
				buf.append(">");
				buf.append(writer.toXml(entry));
				buf.append("</item>");
			}
		}
		buf.append("</pubsub>");
		return buf.toString();
	}

}
