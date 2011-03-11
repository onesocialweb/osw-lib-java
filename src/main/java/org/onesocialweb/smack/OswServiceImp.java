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
package org.onesocialweb.smack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DataForm;
import org.onesocialweb.client.ConnectionStateListener;
import org.onesocialweb.client.Inbox;
import org.onesocialweb.client.OswService;
import org.onesocialweb.client.PresenceListener;
import org.onesocialweb.client.exception.AuthenticationRequired;
import org.onesocialweb.client.exception.ConnectionException;
import org.onesocialweb.client.exception.ConnectionRequired;
import org.onesocialweb.client.exception.RequestException;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.model.relation.Relation;
import org.onesocialweb.model.vcard4.Profile;
import org.onesocialweb.smack.packet.profile.IQProfileProvider;
import org.onesocialweb.smack.packet.profile.IQProfilePublish;
import org.onesocialweb.smack.packet.profile.IQProfileQuery;
import org.onesocialweb.smack.packet.pubsub.IQPubSubItems;
import org.onesocialweb.smack.packet.pubsub.IQPubSubPublish;
import org.onesocialweb.smack.packet.pubsub.IQPubSubRetract;
import org.onesocialweb.smack.packet.pubsub.IQPubSubSubscribe;
import org.onesocialweb.smack.packet.pubsub.IQPubSubSubscribers;
import org.onesocialweb.smack.packet.pubsub.IQPubSubSubscriptions;
import org.onesocialweb.smack.packet.pubsub.IQPubSubUnsubscribe;
import org.onesocialweb.smack.packet.pubsub.MessagePubSubEvent;
import org.onesocialweb.smack.packet.pubsub.MessagePubSubItems;
import org.onesocialweb.smack.packet.pubsub.MessagePubSubRetract;
import org.onesocialweb.smack.packet.pubsub.ProviderPubSubEvent;
import org.onesocialweb.smack.packet.pubsub.ProviderPubSubIQ;
import org.onesocialweb.smack.packet.relation.IQRelationProvider;
import org.onesocialweb.smack.packet.relation.IQRelationQuery;
import org.onesocialweb.smack.packet.relation.IQRelationSetup;
import org.onesocialweb.smack.packet.relation.IQRelationUpdate;
import org.onesocialweb.smack.packet.utils.IQPing;
import org.onesocialweb.xml.writer.ActivityXmlWriter;

public class OswServiceImp implements OswService {
	
	private static final String ACTIVITYSTREAM_NODE = "urn:xmpp:microblog:0";
	
	private static final String REPLYSTREAM_NODE = "urn:xmpp:microblog:0:replies:item=";

	/** The inbox of this logged in session */
	private final Inbox inbox = new InboxImp(this);
	
	/** Keep track of the connection state listeners */
	private List<ConnectionStateListener> connectionStateListeners = new ArrayList<ConnectionStateListener>();
	
	/** Keep track of the presence listeners */
	private List<PresenceListener> presenceListeners = new ArrayList<PresenceListener>();

	/** Keep track of the last present set by this client **/
	private Presence currentPresence;
	
	/** Are we using compression ? **/
	private boolean enableCompression = false;
	
	/** Are we using attempting to reconnect ? **/
	private boolean enableReconnect = true;
		
	/** A reference to the current XMPP connection */
	protected XMPPConnection connection;
	
	@Override
	public String getHostname() {
		if (connection != null) {
			return connection.getHost();
		} else {
			return null;
		}
	}

	@Override
	public String getUser() {
		if (connection != null && connection.isAuthenticated()) {
			return connection.getUser();
		} else {
			return null;
		}
	}
	
	public String getBareJID() {
		String user = getUser();
		if (user != null) {
			if (user.contains("/")) {
				return user.substring(0, user.indexOf('/'));
			} else {
				return user;
			}
		}
		
		return null;
	}

	@Override
	public boolean isAuthenticated() {
		return (connection != null && connection.isAuthenticated());
	}

	@Override
	public boolean isConnected() {
		return (connection != null && connection.isConnected());
	}
	
	@Override
	public void setCompressionEnabled(boolean compressionEnabled) {
		this.enableCompression = compressionEnabled;
	}

	@Override
	public void setReconnectionAllowed(boolean isAllowed) {
		this.enableReconnect = isAllowed;
	}
		
	@Override
	public boolean connect(String server, Integer port, Map<String, String> parameters) throws ConnectionException {
		
		final ConnectionConfiguration config = new ConnectionConfiguration(server, port);
		config.setCompressionEnabled(enableCompression);
		config.setReconnectionAllowed(enableReconnect);
		
		connection = new XMPPConnection(config);

		try {
			connection.connect();
		} catch (XMPPException e) {
			throw new ConnectionException();
		}

		// Add our extension
		ProviderManager.getInstance().addIQProvider("query", "http://onesocialweb.org/spec/1.0/vcard4#query", new IQProfileProvider());
		ProviderManager.getInstance().addIQProvider("query", "http://onesocialweb.org/spec/1.0/relations#query", new IQRelationProvider());
		ProviderManager.getInstance().addIQProvider("pubsub", "http://jabber.org/protocol/pubsub", new ProviderPubSubIQ());
		ProviderManager.getInstance().addExtensionProvider("event",  "http://jabber.org/protocol/pubsub#event", new ProviderPubSubEvent());

		connection.addPacketListener(new MessageListener(), new PacketTypeFilter(Message.class));
		return true;
	}

	@Override
	public boolean disconnect() throws ConnectionRequired {
		requiresConnection();
		connection.disconnect();
		inbox.getEntries().clear();
		return true;
	}
	
	@Override
	public boolean ping() throws ConnectionRequired{
		IQPing ping = new IQPing();
		ping.setType(IQ.Type.GET);
		

		// Send the request and process the reply
		Packet reply = requestBlocking(ping);

		if (reply != null && (reply instanceof IQ)) {
			IQ result = (IQ) reply;
			if (result.getType().equals(IQ.Type.RESULT)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean register(String username, String password, String name, String email) throws ConnectionRequired {
		requiresConnection();

		// Prepare the request
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("username", username);
		attributes.put("password", password);
		attributes.put("email", email);
		attributes.put("name", name);

		Registration query = new Registration();
		query.setType(IQ.Type.SET);
		query.setAttributes(attributes);

		// Send the request and process the reply
		Packet reply = requestBlocking(query);

		if (reply != null && (reply instanceof IQ)) {
			IQ result = (IQ) reply;
			if (result.getType().equals(IQ.Type.RESULT)) {
				return true;
			}
		}

		return false;
	}
	

	
	@Override
	public boolean register(List<FormField> items) throws ConnectionRequired {
		requiresConnection();

		Registration query = new Registration();
		query.setType(IQ.Type.SET);
		DataForm form = new DataForm("submit");
		
		FormField field =new FormField(); 
		field.setType(FormField.TYPE_HIDDEN);
        field.addValue("jabber:iq:register");
        form.addField(field);
        
        Iterator<FormField> it= items.iterator();
        
        for (;it.hasNext();){
        	
        	form.addField(it.next());
        }
		
		
		query.addExtension(form);

		// Send the request and process the reply
		Packet reply = requestBlocking(query);

		if (reply != null && (reply instanceof IQ)) {
			IQ result = (IQ) reply;
			if (result.getType().equals(IQ.Type.RESULT)) {
				return true;
			}
		}

		return false;
	}
	
	@Override
	public DataForm requestForm() throws ConnectionRequired {
		requiresConnection();

		
		Registration query = new Registration();
		query.setType(IQ.Type.GET);	

		// Send the request and process the reply
		Packet reply = requestBlocking(query);

		if (reply != null && (reply instanceof IQ)) {
			IQ result = (IQ) reply;
			if (result.getType().equals(IQ.Type.RESULT)) {
				PacketExtension extension= result.getExtension("jabber:x:data");
				if (extension instanceof DataForm){
					DataForm requiredData= (DataForm)extension;
					 return requiredData;
				}									
			}
		}

		return null;
	}

	@Override
	public boolean deleteActivity(String activityId) throws ConnectionRequired, AuthenticationRequired, RequestException {
		// TODO Auto-generated method stub
		requiresConnection();
		requiresAuth();
						
		IQPubSubRetract packet = new IQPubSubRetract(ACTIVITYSTREAM_NODE, activityId);
		packet.setType(IQ.Type.SET);
		
		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else {
				return true;
			}			
		}
		return false;

	}
	
	
	public boolean updateActivity(ActivityEntry entry) throws ConnectionRequired, AuthenticationRequired, RequestException {
		// TODO Auto-generated method stub
		requiresConnection();
		requiresAuth();
						
		ActivityXmlWriter writer = new ActivityXmlWriter();
		IQPubSubPublish packet = new IQPubSubPublish(ACTIVITYSTREAM_NODE, writer.toXml(entry));
		packet.setType(IQ.Type.SET);
		
		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else {
				return true;
			}			
		}
		return false;

	}
	
	@Override
	public List<ActivityEntry> getReplies(ActivityEntry entry) throws ConnectionRequired, AuthenticationRequired, RequestException{
		
		requiresConnection();
		requiresAuth();

		// Send the request
		IQPubSubItems packet = new IQPubSubItems(REPLYSTREAM_NODE+entry.getId());

		packet.setTo(entry.getActor().getUri());
		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else if (result instanceof IQPubSubItems) {
				IQPubSubItems query = (IQPubSubItems) result;
				return query.getEntries();
			}
		}

		return null;
		
	}

	@Override
	public boolean subscribe(String userJid) throws RequestException, ConnectionRequired, AuthenticationRequired {
		// Validate if we can actually do this
		requiresConnection();
		requiresAuth();

		// Add the user to the roster if not already there
		Roster roster = connection.getRoster();
		RosterEntry rosterEntry = roster.getEntry(userJid);
		if (rosterEntry == null) {
			// Create and send roster entry creation packet.
			RosterPacket.Item item = new RosterPacket.Item(userJid, userJid);
			RosterPacket rosterPacket = new RosterPacket();
			rosterPacket.setType(IQ.Type.SET);
			rosterPacket.addRosterItem(item);
			connection.sendPacket(rosterPacket);
		} 

		// Send the request
		IQPubSubSubscribe request = new IQPubSubSubscribe(ACTIVITYSTREAM_NODE, getBareJID());
		request.setType(IQ.Type.SET);
		request.setTo(userJid);
		IQ reply = requestBlocking(request);

		// Process reply
		if (reply != null) {
			if (reply.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + reply.getError().getCondition());
			} else if (reply.getType() == IQ.Type.RESULT) {
				return true;
			}
		}

		return false;
	}

	@Override
	public List<ActivityEntry> getActivities(String userJid) throws RequestException, ConnectionRequired, AuthenticationRequired {
		// Validate if we can actually do this
		requiresConnection();
		requiresAuth();

		// Send the request
		IQPubSubItems packet = new IQPubSubItems(ACTIVITYSTREAM_NODE);
		packet.setTo(userJid);
		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else if (result instanceof IQPubSubItems) {
				IQPubSubItems query = (IQPubSubItems) result;
				return query.getEntries();
			}
		}

		return null;
	}

	@Override
	public Inbox getInbox() {
		return inbox;
	}

	@Override
	public boolean login(String username, String password, String resource) throws ConnectionRequired, RequestException {
		requiresConnection();

		// Perform the login
		try {
			connection.login(username, password, resource);
			if(connection.isAuthenticated()){
				connection.addConnectionListener(conlistener);
				Roster roster = connection.getRoster();
				roster.addRosterListener(rosterlistener);
			}
			
		} catch (XMPPException e) {
			throw new RequestException(e.getMessage());
		} catch (IllegalStateException e) {
			throw new RequestException(e.getMessage());
		}
		

		// Refresh the inbox
		inbox.refresh();
		fireConnectionStateChanged(ConnectionStateListener.ConnectionState.connected);
		return true;

	}

	@Override
	public boolean postActivity(ActivityEntry entry) throws ConnectionRequired, AuthenticationRequired, RequestException {

		requiresConnection();
		requiresAuth();
		
		ActivityXmlWriter writer = new ActivityXmlWriter();
		IQPubSubPublish packet = new IQPubSubPublish(ACTIVITYSTREAM_NODE, writer.toXml(entry));
		packet.setType(IQ.Type.SET);
		
		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else {
				return true;
			}
			
		}
		return false;
	}
	
	@Override
	public boolean postComment(ActivityEntry entry) throws ConnectionRequired, AuthenticationRequired, RequestException {
		requiresConnection();
		requiresAuth();
		
		ActivityXmlWriter writer = new ActivityXmlWriter();
		IQPubSubPublish packet = new IQPubSubPublish(REPLYSTREAM_NODE + entry.getParentId(), writer.toXml(entry));
		
		packet.setType(IQ.Type.SET);
		
		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else {
				return true;
			}
			
		}
		return false;
	}
	


	@Override
	public boolean unsubscribe(String userJid) throws RequestException, ConnectionRequired, AuthenticationRequired {
		// Validate if we can actually do this
		requiresConnection();
		requiresAuth();
		
		// Send the request
		IQPubSubUnsubscribe request = new IQPubSubUnsubscribe(ACTIVITYSTREAM_NODE, getBareJID());
		request.setType(IQ.Type.SET);
		request.setTo(userJid);
		IQ reply = requestBlocking(request);

		// Process reply
		if (reply != null) {
			if (reply.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + reply.getError().getCondition());
			} else if (reply.getType() == IQ.Type.RESULT) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> getSubscriptions(String jid) throws RequestException, AuthenticationRequired, ConnectionRequired {
		// Validate if we can actually do this
		requiresConnection();
		requiresAuth();

		// Send the request
		IQPubSubSubscriptions packet = new IQPubSubSubscriptions(ACTIVITYSTREAM_NODE);
		if (!jid.equals(getBareJID())) {
			packet.setTo(jid);
		}
		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else if (result instanceof IQPubSubSubscriptions) {
				IQPubSubSubscriptions iq = (IQPubSubSubscriptions) result;
				return iq.getSubscriptions();
			}
		}

		return null;
	}
	
	@Override
	public List<String> getSubscribers(String jid) throws RequestException, AuthenticationRequired, ConnectionRequired {
		// Validate if we can actually do this
		requiresConnection();
		requiresAuth();

		// Send the request
		IQPubSubSubscribers packet = new IQPubSubSubscribers(ACTIVITYSTREAM_NODE);
		if (!jid.equals(getBareJID())) {
			packet.setTo(jid);
		}
		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else if (result instanceof IQPubSubSubscribers) {
				IQPubSubSubscribers iq = (IQPubSubSubscribers) result;
				return iq.getSubscribers();
			}
		}

		return null;
	}
	
	public Roster getRoster(){
		return connection.getRoster();
	}

	public boolean refreshInbox() throws ConnectionRequired, AuthenticationRequired, RequestException {
		// Validate if we can actually do this
		requiresConnection();
		requiresAuth();

		// Send the request
		IQPubSubItems packet = new IQPubSubItems("http://onesocialweb.org/spec/1.0/inbox");
		IQ result = requestBlocking(packet);

		// Process reply
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException(result.getError().getMessage());
			} else if (result instanceof IQPubSubItems) {
				IQPubSubItems iQPubSubItems = (IQPubSubItems) result;
				inbox.setEntries(iQPubSubItems.getEntries());
				return true;
			}
		}

		return false;
	}

	@Override
	public Profile getProfile(String userJid) throws RequestException, AuthenticationRequired, ConnectionRequired {
		// Validate if we can actually do this
		requiresConnection();
		requiresAuth();

		// Send the request
		IQProfileQuery packet = new IQProfileQuery();
		if (userJid != null && userJid.length() > 0) packet.setTo(userJid);
		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else if (result instanceof IQProfileQuery) {
				IQProfileQuery query = (IQProfileQuery) result;
				Profile resultProfile = query.getProfile();
				resultProfile.setUserId(userJid);
				return resultProfile;
			}
		}

		return null;
	}

	@Override
	public boolean setProfile(Profile profile) throws RequestException, AuthenticationRequired, ConnectionRequired {

		requiresConnection();
		requiresAuth();

		IQProfilePublish packet = new IQProfilePublish();
		packet.setProfile(profile);
		packet.setType(IQ.Type.SET);

		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void addConnectionStateListener(ConnectionStateListener listener) {
		connectionStateListeners.add(listener);
	}

	@Override
	public List<Relation> getRelations(String userJid) throws RequestException, ConnectionRequired, AuthenticationRequired {
		// Validate if we can actually do this
		requiresConnection();
		requiresAuth();

		// Send the request
		IQRelationQuery packet = new IQRelationQuery();
		packet.setTo(userJid);
		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else if (result instanceof IQRelationQuery) {
				IQRelationQuery query = (IQRelationQuery) result;
				return query.getRelations();
			}
		}

		return null;
	}
	
	@Override
	public boolean addRelation(Relation relation) throws RequestException, AuthenticationRequired, ConnectionRequired {

		requiresConnection();
		requiresAuth();

		IQRelationSetup packet = new IQRelationSetup();
		packet.setRelation(relation);
		packet.setType(IQ.Type.SET);

		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean updateRelation(Relation relation) throws RequestException, AuthenticationRequired, ConnectionRequired {

		requiresConnection();
		requiresAuth();

		IQRelationUpdate packet = new IQRelationUpdate();
		packet.setRelation(relation);
		packet.setType(IQ.Type.SET);

		IQ result = requestBlocking(packet);

		// Process the request
		if (result != null) {
			if (result.getType() == IQ.Type.ERROR) {
				throw new RequestException("IQ error " + result.getError().getCondition());
			} else {
				return true;
			}
		}
		return false;
	}
	

	@Override
	public String getUploadToken(String requestID) throws RequestException, AuthenticationRequired, ConnectionRequired {
		return connection.getConnectionID();
	}


	@Override
	public void removeConnectionStateListener(ConnectionStateListener listener) {
		connectionStateListeners.remove(listener);
	}
	
	@Override
	public void addPresenceListener(PresenceListener listener) {
		presenceListeners.add(listener);
	}

	@Override
	public void removePresenceListener(PresenceListener listener) {
		presenceListeners.remove(listener);
	}

	@Override
	public void setPresenceMode(Presence.Type pres, Presence.Mode mod){
		currentPresence = new Presence(pres);
		currentPresence.setMode(mod);
		// Send the packet
		connection.sendPacket(currentPresence);
	}
	
	@Override
	public Presence getContactPresence(String user){
		//If the user is the current user just return the currentPresence value defined
		if(user.equals(connection.getUser().split("/")[0])){
			return currentPresence;
		}
		else{
			Roster roster = connection.getRoster();
			return roster.getPresence(user);
		}
	}
	
	private IQ requestBlocking(IQ request) {
		PacketCollector collector = connection.createPacketCollector(new PacketIDFilter(request.getPacketID()));
		connection.sendPacket(request);
		IQ response = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
		collector.cancel();
		return response;
	}

	private void requiresConnection() throws ConnectionRequired {
		if (connection == null || !connection.isConnected()) {
			throw new ConnectionRequired();
		}
	}

	private void requiresAuth() throws AuthenticationRequired {
		if (!connection.isAuthenticated()) {
			throw new AuthenticationRequired();
		}
	}

	private ConnectionListener conlistener =
	  	new ConnectionListener() {
			@Override
			public void connectionClosed() {
				fireConnectionStateChanged(ConnectionStateListener.ConnectionState.disconnected);
			}
			
			@Override
			public void connectionClosedOnError(Exception e) {
				fireConnectionStateChanged(ConnectionStateListener.ConnectionState.disconnectedOnError);
			}

			@Override
			public void reconnectingIn(int seconds) {
			
			}

			@Override
			public void reconnectionFailed(Exception e) {
			
			}

			@Override
			public void reconnectionSuccessful() {
			
			}
		};
		
	private RosterListener rosterlistener = 
		new RosterListener(){

			@Override
			public void entriesAdded(Collection<String> arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void entriesDeleted(Collection<String> arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void entriesUpdated(Collection<String> arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void presenceChanged(Presence p) {
				firePresenceStateChanged(p);
			}
		
	};
	
	private class MessageListener implements PacketListener {
		@Override
		public void processPacket(Packet packet) {
			if (packet instanceof Message) {
				Message message = (Message) packet;				
				MessagePubSubEvent eventPacket = (MessagePubSubEvent) message.getExtension("event", "http://jabber.org/protocol/pubsub#event");
								
				if (eventPacket == null)
					return;
				
				if (eventPacket instanceof MessagePubSubItems) {
					List <ActivityEntry> activities= ((MessagePubSubItems)eventPacket).getEntries();
					if ((activities!=null) && (activities.size()>0)){
						for (ActivityEntry activity : activities) {
							// Search if the activity is already in the inbox
							ActivityEntry previousActivity = inbox.getEntry(activity.getId());
							if (previousActivity != null) {
								inbox.updateEntry(activity);
							} else if ((activity.getParentId()==null)  && (activity.getParentJID()==null)) {
								inbox.addEntry(activity);
							}
						}
					}
				} else if (eventPacket instanceof MessagePubSubRetract) {
					String activityId = ((MessagePubSubRetract) eventPacket).getId();
					ActivityEntry entry = inbox.getEntry(activityId);
					if (entry != null) {
						inbox.removeEntry(entry);
					}
				}
			}
		}
	}
	
	private void fireConnectionStateChanged(ConnectionStateListener.ConnectionState state) {
		for (ConnectionStateListener listener : connectionStateListeners) {
			listener.onStateChanged(state);
		}
	}
	
	private void firePresenceStateChanged(Presence p)
	{
		for (PresenceListener listener : presenceListeners) {
			listener.onPresenceChanged(p);
		}
	}

}
