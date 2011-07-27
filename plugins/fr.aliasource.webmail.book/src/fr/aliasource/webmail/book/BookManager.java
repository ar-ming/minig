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

package fr.aliasource.webmail.book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.utils.RunnableExtensionLoader;
import fr.aliasource.webmail.book.IBookSource.SourceType;
import fr.aliasource.webmail.book.impl.AllContacts;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.LoginUtils;
import fr.aliasource.webmail.proxy.ProxyConfiguration;

/**
 * Address book sources management
 * 
 * @author tom
 * 
 */
public class BookManager {

	public static final String ALL_SOURCE_ID = "all";

	private Log logger;

	private Map<String, IBookSource> groupIndex;
	private List<IBookSource> sources;
	private List<ContactGroup> groups;

	private ProxyConfiguration proxyConfiguration;

	BookManager(ProxyConfiguration proxyConfiguration) {
		this.logger = LogFactory.getLog(getClass());
		this.proxyConfiguration = proxyConfiguration;
		groupIndex = new HashMap<String, IBookSource>();
		sources = new LinkedList<IBookSource>();
		groups = new LinkedList<ContactGroup>();
		registerPluginSources();
		AllContacts allSource = new AllContacts(sources);

		registerSource(allSource);

		logger.info("AddressBook Manager created with " + sources.size()
				+ " source(s) (" + groups.size() + " groups)");
	}

	private void registerPluginSources() {
		RunnableExtensionLoader<IBookSource> rel = new RunnableExtensionLoader<IBookSource>();
		List<IBookSource> bs = rel.loadExtensions(BookActivator.PLUGIN_ID,
				"booksource", "book_source", "implementation");
		for (IBookSource ibs : bs) {
			registerSource(ibs);
		}
	}

	void init() {
	}

	public void registerSource(IBookSource bs) {
		bs.init(proxyConfiguration);
		sources.add(bs);
		ContactGroup cg = bs.getProvidedGroup();
		groups.add(cg);
		groupIndex.put(cg.getId(), bs);
	}

	public List<ContactGroup> getGroups(String userId, String userPassword) {
		List<ContactGroup> ret = new ArrayList<ContactGroup>(groups.size());
		for (ContactGroup g : groups) {
			g.setCount(groupIndex.get(g.getId()).count(userId, userPassword));
			ret.add(g);
		}
		return ret;
	}

	public List<MinigContact> findAll(String userId, String userPassword,
			String groupId) {
		IBookSource bs = groupIndex.get(groupId);
		return bs.findAll(userId, userPassword);
	}

	public List<MinigContact> find(String userId, String userPassword,
			String groupId, String query, int limit) {
		IBookSource bs = groupIndex.get(groupId);
		return bs.find(userId, userPassword, query, limit);
	}

	void shutdown() {
		for (IBookSource a : sources) {
			a.shutdown();
		}
	}

	public void insert(IAccount account, List<MinigContact> c) {
		for (IBookSource bs : sources) {
			if (bs.getType() == SourceType.READ_WRITE) {
				bs
						.insert(LoginUtils.lat(account), account
								.getUserPassword(), c);
			}
		}
	}

}
