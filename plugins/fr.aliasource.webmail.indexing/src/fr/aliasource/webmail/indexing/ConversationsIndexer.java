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

package fr.aliasource.webmail.indexing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.index.core.SearchDirector;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.IConversationListener;
import fr.aliasource.webmail.common.folders.IFolder;

/**
 * Handles update of the email conversations full-text index. Pushed changed
 * conversations data to the {@link SearchDirector}
 * 
 * @author tom
 * 
 */
public class ConversationsIndexer implements IConversationListener {

	private IAccount account;
	private SearchDirector sd;
	private Log logger;

	public ConversationsIndexer(IAccount account, SearchDirector sd) {
		logger = LogFactory.getLog(getClass());
		this.sd = sd;
		this.account = account;
		if (logger.isDebugEnabled()) {
			logger.debug("Conversation listener created for account "
					+ account.getUserId());
		}
	}

	public void conversationCreated(IFolder folder, ConversationReference cref) {
		sd.crawlData(account.getUserId(), cref.getId());
	}

	public void conversationRemoved(IFolder folder, ConversationReference cref) {
		sd.queueDeletion(account.getUserId(), cref.getId());
	}

	public void conversationUpdated(IFolder folder, ConversationReference cref) {
		sd.crawlData(account.getUserId(), cref.getId());
	}

}
