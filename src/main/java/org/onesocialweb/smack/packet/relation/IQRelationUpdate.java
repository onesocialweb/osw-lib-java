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
package org.onesocialweb.smack.packet.relation;

import org.jivesoftware.smack.packet.IQ;
import org.onesocialweb.model.relation.Relation;
import org.onesocialweb.xml.writer.RelationXmlWriter;

public class IQRelationUpdate extends IQ {

	public static String NAME = "update";
	
	public static String NAMESPACE = "http://onesocialweb.org/spec/1.0/relations#update";
	
	private Relation relation;
	
	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	@Override
	public String getChildElementXML() {
		StringBuffer buffer = new StringBuffer();
		RelationXmlWriter writer = new RelationXmlWriter();
		buffer.append("<" + NAME + " xmlns=\"" + NAMESPACE + "\">");
		writer.toXml(relation, buffer);
		buffer.append("</" + NAME + ">");
		return buffer.toString();
	}
}
