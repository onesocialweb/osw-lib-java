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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onesocialweb.client.Inbox;
import org.onesocialweb.client.OswService;
import org.onesocialweb.client.OswServiceFactory;
import org.onesocialweb.model.acl.AclAction;
import org.onesocialweb.model.acl.AclFactory;
import org.onesocialweb.model.acl.AclRule;
import org.onesocialweb.model.acl.AclSubject;
import org.onesocialweb.model.acl.DefaultAclFactory;
import org.onesocialweb.model.vcard4.DefaultVCard4Factory;
import org.onesocialweb.model.vcard4.Field;
import org.onesocialweb.model.vcard4.Profile;
import org.onesocialweb.model.vcard4.VCard4Factory;
import org.onesocialweb.smack.OswServiceFactoryImp;


public class OswServiceImpTest {

	public static final String HOSTNAME = "xmpp.loc";
	
	public static final int PORT = 5222;
	
	public static final String USER = "alice";
	
	public static final String PASSWORD = "password";
	
	private OswServiceFactory serviceFactory = new OswServiceFactoryImp();
	
	@Test
	public void connect() throws Exception {
		OswService service = serviceFactory.createService();
		service.connect(HOSTNAME, PORT, null);
		assertTrue(service.isConnected());
		service.disconnect();
		assertFalse(service.isConnected());
	}
	
	@Test
	public void login() throws Exception {
		OswService service = serviceFactory.createService();
		service.connect(HOSTNAME, PORT, null);
		assertTrue(service.isConnected());
		service.login(USER + "@" + HOSTNAME, PASSWORD, "test-suite");
		assertTrue(service.isAuthenticated());
		service.disconnect();
		assertFalse(service.isAuthenticated());
		assertFalse(service.isConnected());	
	}	
	
	@Test
	public void follow() throws Exception {
		
		// Login
		OswService service = serviceFactory.createService();
		service.connect(HOSTNAME, PORT, null);
		assertTrue(service.isConnected());
		service.login(USER + "@" + HOSTNAME, PASSWORD, "test-suite");
		assertTrue(service.isAuthenticated());
		
		// Follow
		boolean result = service.subscribe("alice@xmpp.loc");
		assertTrue(result);
		
		result = service.unsubscribe("alice@xmpp.loc");
		assertTrue(result);
		
		// Logout
		service.disconnect();
		assertFalse(service.isAuthenticated());
		assertFalse(service.isConnected());			
	}
	
	@Test
	public void inbox() throws Exception {
		// Login
		OswService service = serviceFactory.createService();
		service.connect(HOSTNAME, PORT, null);
		assertTrue(service.isConnected());
		service.login(USER + "@" + HOSTNAME, PASSWORD, "test-suite");
		assertTrue(service.isAuthenticated());
		
		// Inbox
		Inbox inbox = service.getInbox();
		assertNotNull(inbox);
		
		boolean result = inbox.refresh();
		assertTrue(result);
		
		// Logout
		service.disconnect();
		assertFalse(service.isAuthenticated());
		assertFalse(service.isConnected());			
	}
	
	@Test
	public void profile() throws Exception {
		// Login
		OswService service = serviceFactory.createService();
		service.connect(HOSTNAME, PORT, null);
		assertTrue(service.isConnected());
		service.login(USER + "@" + HOSTNAME, PASSWORD, "test-suite");
		assertTrue(service.isAuthenticated());
		
		// Set profile
		VCard4Factory factory = new DefaultVCard4Factory();
		AclFactory aclFactory = new DefaultAclFactory();
		Profile newProfile = factory.profile();
		newProfile.setUserId(USER + "@" + HOSTNAME);
		Field field = factory.photo("http://whatever.com/avatar.jpg");
		AclRule rule = aclFactory.aclRule();
		rule.addAction(aclFactory.aclAction(AclAction.ACTION_VIEW, AclAction.PERMISSION_GRANT));
		rule.addSubject(aclFactory.aclSubject(null, AclSubject.EVERYONE));
		field.addAclRule(rule);
		newProfile.addField(field);
		
		boolean result = service.setProfile(newProfile);
		assertTrue(result);
		
		// Get profile
		Profile userProfile = service.getProfile(USER + "@" + HOSTNAME);
		assertNotNull(userProfile);
		assertEquals(newProfile.toString(), userProfile.toString());
		
		// Logout
		service.disconnect();
		assertFalse(service.isAuthenticated());
		assertFalse(service.isConnected());			
	}
}
