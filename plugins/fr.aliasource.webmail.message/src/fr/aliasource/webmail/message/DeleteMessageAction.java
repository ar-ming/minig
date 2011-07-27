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
 *   minig.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package fr.aliasource.webmail.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;

import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.cache.ConversationCache;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.proxy.IControlledAction;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class DeleteMessageAction implements IControlledAction {

	private Log logger;
	
	public DeleteMessageAction() {
		this.logger = LogFactory.getLog(getClass());
	}

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String convId = req.getParameter("conversation");
		long messUid = Long.parseLong(req.getParameter("message"));
		ConversationCache cc = p.getAccount().getCache().getConversationCache();
		IStoreConnection store = null;

		try {
			store = p.getAccount().getStoreProtocol();

			IFolder f = p.getAccount().getFolder(convId);

			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			logger.info("delete message uid "+messUid+" in conv id : " + convId);
			List<Long> tab = Arrays.asList(messUid);
			store.uidStore(tab, fl, true);
			store.expunge();
			cc.fastUpdate(f, new ArrayList<Long>(0), tab);

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
		return "/deleteMessage.do";
	}

	@Override
	public void init(ProxyConfiguration pcf) {
	}

}
