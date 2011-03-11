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
import java.util.Map;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DataForm;
import org.onesocialweb.client.exception.AuthenticationRequired;
import org.onesocialweb.client.exception.ConnectionException;
import org.onesocialweb.client.exception.ConnectionRequired;
import org.onesocialweb.client.exception.RequestException;
import org.onesocialweb.model.activity.ActivityEntry;
import org.onesocialweb.model.relation.Relation;
import org.onesocialweb.model.vcard4.Profile;


public interface OswService {
 
	/* Connection related commands */
	
    /**
     * Sets if the connection is going to use stream compression. Stream compression
     * will be requested after TLS was established (if TLS was enabled) and only if the server
     * offered stream compression. With stream compression network traffic can be reduced
     * up to 90%. By default compression is disabled.
     *
     * @param compressionEnabled if the connection is going to use stream compression.
     */
	public void setCompressionEnabled(boolean compressionEnabled);
	
    /**
     * Sets if the reconnection mechanism is allowed to be used. By default
     * reconnection is allowed.
     * 
     * @param isAllowed if the reconnection mechanism is allowed to use.
     */
	public void setReconnectionAllowed(boolean isAllowed);
	
	/**
	 * Connects to the specified XMPP server
	 * 
	 * @param server       				hostname of the XMPP server to connect to.
	 * @param port         				port number in which the XMPP Server is running.
	 * @param parameters   				other connection parameters
	 * @return             				true if the connection was successful, false otherwise.
	 * @throws ConnectionException		throws a ConnectionException if it doesn't succeed in 
	 * 									establishing the XMPP Connection
	 */	
	public boolean connect(String server, Integer port, Map<String, String> parameters) throws ConnectionException;

	
	/**
	 * Disconnects from the XMPP server
	 * 
	 * @return  true if it succeeds in disconnecting, false otherwise.
	 * @throws ConnectionRequired   will throw an exception if the service was not connected.
	 */
	public boolean disconnect() throws ConnectionRequired;

	
	/**
	 * Register a new user at the connected XMPP server (by sending a Registration IQ)
	 * 
	 * @param username 				jid of the user to register.
	 * @param password  			password for the user at the connected server.
	 * @param name					name of the user to register.
	 * @param email     			email address of the user to register. 
	 * @return          			true if it succeeded in registering the user, i.e. receives 
	 * 								a reply IQ of type RESULT.
	 * @throws ConnectionRequired  	will throw an exception if the service was not connected.
	 */
	public boolean register(String username, String password, String name, String email) throws ConnectionRequired;
	
	public boolean register(List<FormField> fields) throws ConnectionRequired;
	
	public boolean ping() throws ConnectionRequired;
		
	
	public DataForm requestForm() throws ConnectionRequired;

	
	/**
	 * Authenticates the user at the connected XMPP Server and tries to retrieve its roster and its inbox of Activities.
	 * 
	 * @param username				jid of the user that wants to login.
	 * @param password				user's password in the server.
	 * @param resource				specific jabber resource from which the user will be connected.
	 * @return						true if it succeeded in login the user into the server.
	 * @throws ConnectionRequired  	will throw a ConnectionRequired exception if the service was not connected.
	 * @throws RequestException		will throw a RequestException if the XMPP connection fails to login the user.
	 */
	public boolean login(String username, String password, String resource) throws ConnectionRequired, RequestException;

	
	/**
	 * @return	true if the XMPP connection is not null (i.e. the connection to XMPP Server has been established)
	 * 			and a user has already logged in by using the login method. Returns false otherwise.
	 */
	boolean isAuthenticated();
	
	
	/**
	 * @return  true if the connection is not null and is currently connected to an XMPP server.
	 */
	boolean isConnected();
	
		
	/**
	 * @return	the jabberId of the user that was authenticated using the login method.
	 * 			Returns null if no user was authenticated yet.
	 * 			
	 */
	public String getUser();
	
	
	
	/**
	 * @return	the host name of the XMPP Server currently connected to. 
	 * 			Returns null if currently not connected.
	 */
	public String getHostname();
	
	
	/**
	 * Adds a listener to the connection that will be notified when the state 
	 * of the connection (connected, disconnectedOnError or disconnected) changes.
	 * 
	 * @param listener	
	 */
	public void addConnectionStateListener(ConnectionStateListener listener);
	
	
	
	/**
	 * Removes the specified connection state listener from the service.
	 * 
	 * @param listener
	 */
	public void removeConnectionStateListener(ConnectionStateListener listener);
	

	/* Activity stream related commands */

	
	/**
	 * Subscribe to the activity stream of another user, who will be added to the roster. ("Follow").
	 * 
	 * @param userJid   				jid of the user to subscribe to
	 * @return							true if it succeeds, i.e. receives a reply IQ of type RESULT. 
	 * @throws RequestException			throws a RequestException if it receives a reply IQ of type ERROR after 
	 * 									attempting to send a subscription IQ to the server.
	 * @throws ConnectionRequired		throws a ConnectionRequired exception if not connected to an XMPP Server.
	 * @throws AuthenticationRequired	throws an AuthenticationRequired is no user has been logged in yet.
	 */
	public boolean subscribe(String userJid) throws RequestException, ConnectionRequired, AuthenticationRequired;

	
	/**
	 * Unsubscribe to the activity stream of another user. ("Unfollow").
	 * 
	 * @param userJid					jid of the user to unsubscribe from.
	 * @return							true if it succeeded in unsubscribing from a user, 
	 * 									i.e. if it receives a reply IQ of type RESULT.
	 * @throws RequestException			throws a RequestException if it receives a reply IQ of type ERROR after 
	 * 									attempting to send a unsubscription IQ to the server.
	 * @throws ConnectionRequired		throws a ConnectionRequired exception if not connected to an XMPP Server.
	 * @throws AuthenticationRequired	throws an AuthenticationRequired is no user has been logged in yet.
	 */
	public boolean unsubscribe(String userJid) throws RequestException, ConnectionRequired, AuthenticationRequired;
	
	
	
	/**
	 * Queries the list of users that are currently subscribed to the given jid
	 * 
 	 * @param userJid					jid of the user to fetch subscribers for
	 * @return							a List of jids.
	 * @throws RequestException			throws a RequestException if it receives a reply IQ of type ERROR after 
	 * 									attempting to retrieve the list of subscribers from the server.
	 * @throws ConnectionRequired		throws a ConnectionRequired exception if not connected to an XMPP Server.
	 * @throws AuthenticationRequired	throws an AuthenticationRequired is no user has been logged in yet.
	 */
	public List<String> getSubscribers(String jid) throws RequestException, ConnectionRequired, AuthenticationRequired;
	
	
	/**
	 * Queries the list of users that the give jid is subscribed to
	 * 
	 * @param userJid					jid of the user to fetch subscriptions for
	 * @return							a List of jids.
	 * @throws RequestException			throws a RequestException if it receives a reply IQ of type ERROR after 
	 * 									attempting to retrieve the list of subscriptions from the server.
	 * @throws ConnectionRequired		throws a ConnectionRequired exception if not connected to an XMPP Server.
	 * @throws AuthenticationRequired	throws an AuthenticationRequired is no user has been logged in yet.
	 */
	public List<String> getSubscriptions(String jid) throws RequestException, ConnectionRequired, AuthenticationRequired;

	
	/**
	 * Get the OneSocialWeb Inbox encapsulated in the OswService
	 * 
	 * @return		returns the inbox of the service, which contains a list of activity entries. 
	 * 				The inbox is refreshed upon actions such as OswService login and disconnect.
	 */
	public Inbox getInbox();
	
	public Roster getRoster();
	
	
	/**
	 * Retrieves from the XMPP Server the List of Activities (ActivityEntries) of the specified user.
	 * 
	 * @param userJid					The jid of the user whose activities we want to retrieve.
	 * @return							A List of ActivityEntries. See: http://onesocialweb.org/spec/1.0/xep-osw-activities.html
	 * @throws RequestException			throws a RequestException if it receives from the server a reply IQ  of
	 * 									type ERROR.
	 * @throws ConnectionRequired		throws a ConnectionRequired exception if not connected to an XMPP Server.
	 * @throws AuthenticationRequired	throws an AuthenticationRequired is no user has been logged in yet.
	 */
	public List<ActivityEntry> getActivities(String userJid) throws RequestException, ConnectionRequired, AuthenticationRequired;

	
	
	/**
	 * @param entry							An ActivityEntry. See: http://onesocialweb.org/spec/1.0/xep-osw-activities.html
	 * @return								true if it succeeded in posting the activity to the server 
	 * 										i.e. if it receives a reply IQ of type RESULT. 
	 * @throws ConnectionRequired			throws a ConnectionRequired exception if not connected to an XMPP Server.
	 * @throws AuthenticationRequired  		throws an AuthenticationRequired is no user has been logged in yet.
	 * @throws RequestException				throws a RequestException if it receives from the server a reply IQ  of
	 * 										type ERROR.
	 */
	public boolean postActivity(ActivityEntry entry) throws ConnectionRequired, AuthenticationRequired, RequestException;
	
	/**
	 * @param entry							An ActivityEntry. See: http://onesocialweb.org/spec/1.0/xep-osw-activities.html
	 * @return								true if it succeeded in posting the comment to the server 
	 * 										i.e. if it receives a reply IQ of type RESULT. 
	 * @throws ConnectionRequired			throws a ConnectionRequired exception if not connected to an XMPP Server.
	 * @throws AuthenticationRequired  		throws an AuthenticationRequired is no user has been logged in yet.
	 * @throws RequestException				throws a RequestException if it receives from the server a reply IQ  of
	 * 										type ERROR.
	 */
	public boolean postComment(ActivityEntry entry) throws ConnectionRequired, AuthenticationRequired, RequestException;

	
	/**
	 * Tries to delete the activity (if posted by the user) and to notify all subscribers. 
	 * If the activity was not posted by the user in the first place, it will throw 
	 * 
	 * @param activityId					An ActivityId. See: http://onesocialweb.org/spec/1.0/xep-osw-activities.html
	 * @return								true if it succeeded in deleting the activity at the server 
	 * 										i.e. if it receives a reply IQ of type RESULT. 
	 * @throws ConnectionRequired			throws a ConnectionRequired exception if not connected to an XMPP Server.
	 * @throws AuthenticationRequired  		throws an AuthenticationRequired is no user has been logged in yet.
	 * @throws RequestException				throws a RequestException if it receives from the server a reply IQ  of
	 * 										type ERROR.
	 */
	public boolean deleteActivity(String activityId) throws ConnectionRequired, AuthenticationRequired, RequestException;

	
	/**
	 * @param activityId					An ActivityEntry. See: http://onesocialweb.org/spec/1.0/xep-osw-activities.html
	 * @return								true if it succeeded in updating the activity at the server 
	 * 										i.e. if it receives a reply IQ of type RESULT. 
	 * @throws ConnectionRequired			throws a ConnectionRequired exception if not connected to an XMPP Server.
	 * @throws AuthenticationRequired  		throws an AuthenticationRequired is no user has been logged in yet.
	 * @throws RequestException				throws a RequestException if it receives from the server a reply IQ  of
	 * 										type ERROR.
	 */
	public boolean updateActivity(ActivityEntry entry) throws ConnectionRequired, AuthenticationRequired, RequestException;
	
	/* Profile related commands */
	
	public List<ActivityEntry> getReplies(ActivityEntry entry) throws ConnectionRequired, AuthenticationRequired, RequestException;
	
	
	/**
	 * Retrieves from the XMPP server a complete profile by trying to send an IQ of type IQProfileQuery.
	 * 
	 * @param userJid					jid of the user whose profile we want to retrieve.
	 * @return							the OneSocialWeb Profile. See: http://onesocialweb.org/spec/1.0/xep-osw-profile.html
	 * @throws RequestException			throws a RequestException if it receives from the server a reply IQ  of
	 * 									type ERROR after trying to retrieve the Profile.
	 * @throws ConnectionRequired		throws a ConnectionRequired exception if not connected to an XMPP Server.
	 * @throws AuthenticationRequired	throws an AuthenticationRequired is no user has been logged in yet.
	 */
	public Profile getProfile(String userJid) throws RequestException, ConnectionRequired, AuthenticationRequired;
	
	/**
	 * Set or Publish a Profile to the XMPP server by sending an IQ of type IQProfilePublish
	 * @param profile					the OneSocialWeb Profile to be set in the server. 
	 * 									See: http://onesocialweb.org/spec/1.0/xep-osw-profile.html
	 * @return							true if it succeeded in posting the profile to the server 
	 * 									i.e. if it receives a reply IQ of type RESULT. 					
	 * @throws RequestException 		throws a RequestException if it receives from the server a reply IQ  of
	 * 									type ERROR after trying to set the Profile.
	 * @throws AuthenticationRequired	throws an AuthenticationRequired is no user has been logged in yet.
	 * @throws ConnectionRequired		throws a ConnectionRequired exception if not connected to an XMPP Server.
	 */
	public boolean setProfile(Profile profile) throws RequestException, AuthenticationRequired, ConnectionRequired;

	/* Relation related commands */

	/**
	 * Retrieves from the XMPP Server the List of Relations of a user by sending an IQ of type IQRelationQuery
	 *  
	 * @param userJid					the JabberId of the user whose relations we want to retrieve.
	 * @return							List of Relations. See: http://onesocialweb.org/spec/1.0/xep-osw-relations.html
	 * @throws RequestException			throws a RequestException if it receives from the server a reply IQ  of
	 * 									type ERROR after trying to retrieve the Relations
	 * @throws ConnectionRequired		throws a ConnectionRequired exception if not connected to an XMPP Server.
	 * @throws AuthenticationRequired	throws an AuthenticationRequired is no user has been logged in yet.
	 */
	public List<Relation> getRelations(String userJid) throws RequestException, ConnectionRequired, AuthenticationRequired;
	
    /**
     * Set a new Relation to the server by trying to send an IQ of type IQRelationSetup.
     * 
     * @param relation					The social Relation object to be set in the server. 
     * 									See: http://onesocialweb.org/spec/1.0/xep-osw-relations.html
     * @return							true if no error ocurred.
     * @throws RequestException			throws a RequestException if it receives from the server a reply IQ of
	 * 									type ERROR after trying to add the Relations.
     * @throws AuthenticationRequired	throws an AuthenticationRequired is no user has been logged in yet.
     * @throws ConnectionRequired		throws a ConnectionRequired exception if not connected to an XMPP Server.
     */
    public boolean addRelation(Relation relation)  throws RequestException, AuthenticationRequired, ConnectionRequired;

    
    /**
     * Updates an existing Social Relation in the server by sending an IQ of type IQRelationUpdate
     * 
     * @param relation					The social Relation object to be updated in the server.
     * @return							true if no error ocurred.
     * @throws RequestException			throws a RequestException if it receives from the server a reply IQ of
	 * 									type ERROR after trying to add the Relation.
     * @throws AuthenticationRequired	throws an AuthenticationRequired is no user has been logged in yet.
     * @throws ConnectionRequired		throws a ConnectionRequired exception if not connected to an XMPP Server.
     */	
    public boolean updateRelation(Relation relation)  throws RequestException, AuthenticationRequired, ConnectionRequired;

    /* XMPP Presence related commands */
	
	/**
	 * Send to the XMPP Server a Presence Stanza containing a new type of Presence for this user+resource
	 * 
	 * @param pres						A Presence.Type representing an available or unavailable presence.
	 * @param mod						A Presence.Mode representing a value out of the following: 
	 * 									available  (the default), chat, away, xa  (extended away), 
	 * 									and dnd  (do not disturb). 
	 */
	public void setPresenceMode(Type pres, Mode mod);
	
	
	/**
	 * Obtains the presence of the current user (if the same user as connected is specified), else it will try
	 * to obtain the presence information of the specified user from the roster.
	 * 
	 * @param user					jid of the user whose presence we want to retrieve.	
	 * @return						A Presence.Type or null if the Presence couldn't be obtained.
	 */
	public Presence getContactPresence(String user);

	/**
	 * Adds a listener to the connection that will be notified when the presence  
	 * of the authenticated user (available / unavailable) changes.
	 * 
	 * @param listener
	 */
	public void addPresenceListener(PresenceListener listener);

	/**
	 * Removes the specified Presence listener from the service.
	 * 
	 * @param listener
	 */
	public void removePresenceListener(PresenceListener listener);
	
	/* File upload */
    

    public String getUploadToken(String requestID)  throws RequestException, AuthenticationRequired, ConnectionRequired;
    
}