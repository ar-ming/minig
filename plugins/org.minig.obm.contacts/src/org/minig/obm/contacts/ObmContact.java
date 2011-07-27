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

package org.minig.obm.contacts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.minig.obmsync.service.IBookService;

import fr.aliasource.webmail.book.BookActivator;
import fr.aliasource.webmail.book.BookManager;
import fr.aliasource.webmail.book.ContactGroup;
import fr.aliasource.webmail.book.MinigContact;

/**
 * AddressBook implementation for contacts from obm.
 * 
 * @author tom
 * 
 */
public class ObmContact extends OBMAddressBook {

	public ObmContact() {
	}

	@Override
	public ContactGroup getProvidedGroup() {
		return new ContactGroup("obm_contact", "Contacts");
	}

	@Override
	public SourceType getType() {
		return SourceType.READ_WRITE;
	}

	@Override
	public void insert(String userId, String userPassword, List<MinigContact> cl) {
		try {
			IBookService bookService = getBookService(userId, userPassword);

			List<MinigContact> toInsert = new ArrayList<MinigContact>(cl.size());
			for (MinigContact c : cl) {
				List<MinigContact> l = new LinkedList<MinigContact>();
				for (String lbl : c.getEmails().keySet()) {
					l.addAll(BookActivator.getDefault().getBookManager().find(
							userId, userPassword, BookManager.ALL_SOURCE_ID,
							c.getEmails().get(lbl).getEmail(), 1));
				}
				if (l.size() == 0) {
					logger.info("will insert " + c.getLastname());
					toInsert.add(c);
				} else {
					logger.info("skip " + c.getLastname());
				}
			}
			bookService.insert(toInsert);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
