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
package org.onesocialweb.smack.packet.profile;

import org.jivesoftware.smack.packet.IQ;
import org.onesocialweb.model.vcard4.Profile;
import org.onesocialweb.xml.writer.VCard4XmlWriter;

public class IQProfileQuery extends IQ {
	
	public static String NAME = "query";
	
	public static String NAMESPACE = "http://onesocialweb.org/spec/1.0/vcard4#query";
	
	private Profile profile;
	
	public Profile getProfile() {
		return this.profile;
	}
	
	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	@Override
	public String getChildElementXML() {
		final StringBuffer buffer = new StringBuffer();

		buffer.append("<" + NAME + " xmlns=\"" + NAMESPACE + "\">");
		if (profile != null) {
			final VCard4XmlWriter writer = new VCard4XmlWriter();
			writer.toXml(profile);
		}
		buffer.append("</" + NAME + ">");
		return buffer.toString();
	}
}
