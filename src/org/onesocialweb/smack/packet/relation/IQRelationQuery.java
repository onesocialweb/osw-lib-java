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

import java.util.List;

import org.jivesoftware.smack.packet.IQ;
import org.onesocialweb.model.relation.Relation;
import org.onesocialweb.xml.writer.RelationXmlWriter;

public class IQRelationQuery extends IQ {

	public static String NAME = "query";
	
	public static String NAMESPACE = "http://onesocialweb.org/spec/1.0/relations#query";
	
	private List<Relation> relations;
	
	public List<Relation> getRelations() {
		return relations;
	}

	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}

	@Override
	public String getChildElementXML() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<" + NAME + " xmlns=\"" + NAMESPACE + "\">");
		if (relations != null && relations.size() > 0) {
			final RelationXmlWriter writer = new RelationXmlWriter();
			for (Relation relation : relations) {
				writer.toXml(relation, buffer);
			}
		}
		buffer.append("</" + NAME + ">");
		return buffer.toString();
	}
}
