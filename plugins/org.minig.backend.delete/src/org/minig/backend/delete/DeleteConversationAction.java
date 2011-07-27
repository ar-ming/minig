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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;

import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.cache.ConversationCache;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class DeleteConversationAction extends AbstractControlledAction {

	private Log logger = LogFactory.getLog(getClass());

	public DeleteConversationAction() {
	}

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String commaList = req.getParameter("conversations");
		String[] convIds = commaList.split(",");
		Arrays.sort(convIds);
		Set<IFolder> folders = new HashSet<IFolder>();
		ConversationCache cc = p.getAccount().getCache().getConversationCache();
		IStoreConnection store = null;
		HashMap<IFolder, Collection<Long>> hm = new HashMap<IFolder, Collection<Long>>();

		try {
			store = p.getAccount().getStoreProtocol();

			ConversationReference cr = null;
			for (String cid : convIds) {
				if (cid == null || cid.trim().length() == 0) {
					continue;
				}

				cr = cc.find(cid);
				IFolder f = p.getAccount().getFolder(cid);
				folders.add(f);

				Collection<Long> uidSeq = cr.getUidSequence();
				if (hm.containsKey(f)) {
					hm.put(f, mergeIds(hm.get(f), uidSeq));
				} else {
					hm.put(f, uidSeq);
				}

				logger.info("delete conv id : " + cid);
			}

			for (IFolder folder : hm.keySet()) {
				Collection<Long> toDelIds = hm.get(folder);
				FlagsList fl = new FlagsList();
				fl.add(Flag.DELETED);
				store.select(folder.getName());
				logger.info("will set flag DELETED on " + toDelIds.size()
						+ " in folder " + folder.getName());
				store.uidStore(toDelIds, fl, true);
				store.expunge();
				cc.fastUpdate(folder, new ArrayList<Long>(0), toDelIds);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (store != null) {
				store.destroy();
			}
		}
	}

	private List<Long> mergeIds(Collection<Long> currentIds, Collection<Long> newIds) {
		List<Long> ret = new ArrayList<Long>(currentIds.size() + newIds.size());
		ret.addAll(currentIds);
		ret.addAll(newIds);
		return ret;
	}

	@Override
	public String getUriMapping() {
		return "/deleteConversation.do";
	}

}
