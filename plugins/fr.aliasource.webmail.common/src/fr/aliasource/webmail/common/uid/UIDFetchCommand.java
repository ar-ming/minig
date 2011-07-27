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

package fr.aliasource.webmail.common.uid;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.SearchQuery;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.cache.IDirectCommand;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.imap.StoreException;

public class UIDFetchCommand implements IDirectCommand<Set<Long>> {

	private IAccount ac;
	private Log logger;
	private String folder;

	public UIDFetchCommand(IFolder folder, IAccount ac) {
		logger = LogFactory.getLog(getClass());
		this.ac = ac;
		this.folder = folder.getName();
	}

	@Override
	public Set<Long> getData() throws IOException, StoreException,
			InterruptedException {
		Set<Long> ret = new HashSet<Long>();
		IStoreConnection imp = ac.getStoreProtocol();
		try {
			boolean select = imp.select(folder);
			if (select) {
				Collection<Long> uids = imp.uidSearch(new SearchQuery());
				for (Long uid : uids) {
					ret.add(uid);
				}
			} else {
				logger.warn("Failed to select " + folder + " with account "
						+ ac.getUserId() + ". Returning empty uid list");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			imp.destroy();
		}

		return ret;
	}

}
