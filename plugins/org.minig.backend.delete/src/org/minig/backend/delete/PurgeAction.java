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

package org.minig.backend.delete;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.SearchQuery;

import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.cache.ConversationCache;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class PurgeAction extends AbstractControlledAction {

	private Log logger = LogFactory.getLog(getClass());

	public PurgeAction() {
	}

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		IFolder toPurge = new IMAPFolder(req.getParameter("folder"));
		ConversationCache cc = p.getAccount().getCache().getConversationCache();
		IStoreConnection store = null;

		try {
			store = p.getAccount().getStoreProtocol();
			store.select(toPurge.getName());
			Collection<Long> allUids = store.uidSearch(new SearchQuery());

			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			store.uidStore(allUids, fl, true);
			store.expunge();
			cc.fastUpdate(toPurge, new ArrayList<Long>(0), allUids);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (store != null) {
				store.destroy();
			}
		}
	}

	@Override
	public String getUriMapping() {
		return "/purge.do";
	}

}
