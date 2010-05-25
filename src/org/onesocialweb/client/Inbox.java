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
package org.onesocialweb.client;

import java.util.List;

import org.onesocialweb.model.activity.ActivityEntry;

public interface Inbox {
	
	public ActivityEntry getEntry(String id);
	
	public List<ActivityEntry> getEntries();
	
	public void setEntries(List<ActivityEntry> entries);
	
	public void addEntry(ActivityEntry entry);
	
	public void removeEntry(ActivityEntry entry);
	
	public void updateEntry(ActivityEntry entry);
	
	public boolean refresh();
	
	public void registerInboxEventHandler(InboxEventHandler handler);
	
	public void unregisterInboxEventHandler(InboxEventHandler handler);
	
	public int getSize();

}
