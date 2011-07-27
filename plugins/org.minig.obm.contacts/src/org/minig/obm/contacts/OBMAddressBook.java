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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obmsync.exception.ObmSyncConnectionException;
import org.minig.obmsync.service.BookService;
import org.minig.obmsync.service.IBookService;

import fr.aliasource.webmail.book.MinigContact;
import fr.aliasource.webmail.book.IBookSource;
import fr.aliasource.webmail.proxy.ProxyConfiguration;

public abstract class OBMAddressBook implements IBookSource {

	protected Log logger = LogFactory.getLog(getClass());

	protected OBMAddressBook() {
	}

	public int count(String userId, String userPassword) {
		int count = 0;

		try {
			IBookService bookService = getBookService(userId, userPassword);
			count = bookService.count();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return count;
	}

	public List<MinigContact> find(String userId, String userPassword, String query,
			int limit) {

		try {
			IBookService bookService = getBookService(userId, userPassword);
			
			List<MinigContact> ret = bookService.find( query, limit);
			
			logger.info(userId + ", query: " + query + ", "
					+ " => " + ret.size() + " contacts.");
			
			return ret;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		
		
		return new LinkedList<MinigContact>();
	}

	@Override
	public List<MinigContact> findAll(String userId, String userPassword) {
		try {
			IBookService bookService = getBookService(userId, userPassword);
			return bookService.findAll();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new LinkedList<MinigContact>();
	}

	protected IBookService getBookService(String userId, String userPassword)
			throws ObmSyncConnectionException {
		return new BookService(userId, userPassword);
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		// con.close();
	}

	@Override
	public void init(ProxyConfiguration pcf) {
		logger.info("initialising obm ab.");
	}

}
