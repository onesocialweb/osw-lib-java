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
 *  2011-01-16 Modified by Luca Faggioli Copyright 2010 Openliven S.r.l
 *  
 *  added notification of new Comments 
 *    
 */
package org.onesocialweb.client;

import java.util.List;

import org.onesocialweb.model.activity.ActivityEntry;

public interface InboxEventHandler {

	public void onMessageReceived(ActivityEntry entry);
	
	public void onMessageDeleted(ActivityEntry entry);
	
	public void onMessageUpdated(ActivityEntry entry);
	
	public void onRefresh(List<ActivityEntry> activities);
	
	public void onReplyAdded(ActivityEntry entry);
	
}
