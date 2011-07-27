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

package fr.aliasource.webmail.book.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.book.BookManager;
import fr.aliasource.webmail.book.MinigContact;
import fr.aliasource.webmail.book.ContactGroup;
import fr.aliasource.webmail.book.IBookSource;
import fr.aliasource.webmail.proxy.ProxyConfiguration;

public class AllContacts implements IBookSource {

	private final ContactGroup allCg;
	private Set<IBookSource> otherSources;
	private Log logger = LogFactory.getLog(getClass());

	public AllContacts(Collection<IBookSource> allSources) {
		this.otherSources = new HashSet<IBookSource>();
		otherSources.addAll(allSources);
		allCg = new ContactGroup(BookManager.ALL_SOURCE_ID, "All contacts");
		logger.info("AllContacts virtual source created  for "+otherSources.size() +" sources");
	}

	@Override
	public ContactGroup getProvidedGroup() {
		return allCg;
	}

	@Override
	public SourceType getType() {
		return SourceType.READ_ONLY;
	}

	@Override
	public void init(ProxyConfiguration pc) {
		if (logger.isDebugEnabled()) {
			logger.debug("init");
		}
	}

	@Override
	public void shutdown() {
		if (logger.isDebugEnabled()) {
			logger.debug("shutdown");
		}
	}

	@Override
	public List<MinigContact> findAll(String userId, String userPassword) {
		List<MinigContact> ret = new LinkedList<MinigContact>();
		for (IBookSource bs : otherSources) {
			ret.addAll(bs.findAll(userId, userPassword));
		}

		Collections.sort(ret);
		return ret;
	}

	@Override
	public List<MinigContact> find(String userId, String userPassword, String query, int limit) {
		List<MinigContact> ret = new LinkedList<MinigContact>();
		for (IBookSource bs : otherSources) {
			ret.addAll(bs.find(userId, userPassword, query, limit));
		}

		Collections.sort(ret);
		return ret;
	}

	@Override
	public int count(String userId, String userPassword) {
		int count = 0;
		for (IBookSource bs : otherSources) {
			count += bs.count(userId, userPassword);
		}
		return count;
	}

	@Override
	public void insert(String userId, String userPassword, List<MinigContact> c) {
		// TODO Auto-generated method stub
		
	}

}
