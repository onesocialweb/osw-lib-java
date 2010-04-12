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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.xml.xpp.imp.DefaultXppActivityReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ProviderPubSubIQ implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser parser) throws Exception {	

		// Process the child element
        if (parser.nextTag() == XmlPullParser.START_TAG) {

            String childElement = parser.getName();

            if (childElement.equals("publish")) {
                return parsePublish(parser);
            } else if (childElement.equals("items")) {
            	return parseItems(parser);
            } else if (childElement.equals("subscribe")) {
            	return parseSubscribe(parser);
            } else if (childElement.equals("unsubscribe")) {
            	return parseUnsubscribe(parser);
            } else if (childElement.equals("subscriptions")) {
            	return parseSubscriptions(parser);
            } else if (childElement.equals("subscribers")) {
            	return parseSubscribers(parser);
            }
        }
        
        return null;
	}
	
	private IQ parsePublish(XmlPullParser parser) {
		String node = parser.getAttributeValue(null, "node");
		return new IQPubSubPublish(node, null);
	}
	
	private IQ parseItems(XmlPullParser parser) throws IOException, XmlPullParserException {
		final DefaultXppActivityReader reader = new DefaultXppActivityReader();
		final List<ActivityEntry> entries = new ArrayList<ActivityEntry>();
		final String node = parser.getAttributeValue(null, "node");
		boolean done = false;

		while (!done) {
			int eventType = parser.next();
			if (eventType == XmlPullParser.START_TAG) {
				if (parser.getName().equals("entry")) {
					entries.add(reader.parse(parser));
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if (parser.getName().equals("pubsub")) {
					done = true;
				}
			}
		}

		IQPubSubItems iq = new IQPubSubItems(node);
		iq.setEntries(entries);

		return iq;
	}

	private IQ parseSubscribe(XmlPullParser parser) {
		String node = parser.getAttributeValue(null, "node");
		return new IQPubSubSubscribe(node, null);
	}
	
	private IQ parseUnsubscribe(XmlPullParser parser) {
		String node = parser.getAttributeValue(null, "node");
		return new IQPubSubUnsubscribe(node, null);
	}
	
	private IQ parseSubscriptions(XmlPullParser parser) throws IOException, XmlPullParserException {
		final List<String> subscriptions = new ArrayList<String>();
		final String node = parser.getAttributeValue(null, "node");
		boolean done = false;

		while (!done) {
			int eventType = parser.next();
			if (eventType == XmlPullParser.START_TAG) {
				if (parser.getName().equals("subscription")) {
					String jid = parser.getAttributeValue(null, "jid");
					if (jid != null) subscriptions.add(jid);
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if (parser.getName().equals("pubsub")) {
					done = true;
				}
			}
		}

		IQPubSubSubscriptions iq = new IQPubSubSubscriptions(node);
		iq.setSubscriptions(subscriptions);

		return iq;
	}
	
	private IQ parseSubscribers(XmlPullParser parser) throws IOException, XmlPullParserException {
		final List<String> subscribers = new ArrayList<String>();
		final String node = parser.getAttributeValue(null, "node");
		boolean done = false;

		while (!done) {
			int eventType = parser.next();
			if (eventType == XmlPullParser.START_TAG) {
				if (parser.getName().equals("subscriber")) {
					String jid = parser.getAttributeValue(null, "jid");
					if (jid != null) subscribers.add(jid);
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if (parser.getName().equals("pubsub")) {
					done = true;
				}
			}
		}

		IQPubSubSubscribers iq = new IQPubSubSubscribers(node);
		iq.setSubscribers(subscribers);

		return iq;
	}
}
