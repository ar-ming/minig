/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.minig.obmsync.provider.impl;

import java.util.LinkedList;
import java.util.List;

import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.client.book.BookClient;

import fr.aliasource.webmail.book.MinigContact;
import fr.aliasource.webmail.book.MinigContactFactory;

public class ObmSyncBookProvider  extends AbstractProvider {

	private BookClient book;

	public ObmSyncBookProvider(BookClient book) {
		this.book = book;
	}

	public void login(String userId, String userPassword) {
		this.token = book.login(userId, userPassword, "minig");
	}

	public void logout() {
		if(this.token != null){
			book.logout(token);
		}
	}

	public void create(MinigContact contact) throws AuthFault, ServerFault {
		book.createContact(token, BookType.contacts, getObmSyncContact(contact));
	}
	
	public List<MinigContact> find(String query, int limit) throws AuthFault, ServerFault {
		return getListContact(book.searchContact(token, query, limit));
	}

	public List<MinigContact> getAll() throws AuthFault, ServerFault {
		return find("", Integer.MAX_VALUE);
	}
	
	public int count() throws AuthFault, ServerFault {
		return getAll().size();
	}
	
	
	private MinigContact getMinigContact(org.obm.sync.book.Contact obmSyncContact){
		return new MinigContactFactory().createFrom(obmSyncContact);
	}
	
	private List<MinigContact> getListContact(List<org.obm.sync.book.Contact> listObmSyncContact){
		List<MinigContact> contacts = new LinkedList<MinigContact>();
		for(org.obm.sync.book.Contact c : listObmSyncContact ){
			contacts.add(getMinigContact(c));
		}
		return contacts;
 	}
	
	
	private Contact getObmSyncContact(MinigContact minigContact){
		return minigContact.adapt(Contact.class);
	}
	
	@SuppressWarnings("unused")
	private List<org.obm.sync.book.Contact> getListObmSyncContact(List<MinigContact> listMinigContact){
		List<org.obm.sync.book.Contact> contacts = new LinkedList<org.obm.sync.book.Contact>();
		for(MinigContact c : listMinigContact ){
			contacts.add(getObmSyncContact(c));
		}
		return contacts;
 	}
	
}
