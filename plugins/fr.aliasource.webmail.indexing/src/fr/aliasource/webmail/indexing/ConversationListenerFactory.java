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

import fr.aliasource.index.core.AbstractCrawler;
import fr.aliasource.index.core.ICrawlerFactory;
import fr.aliasource.index.core.IIndexingParameters;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.conversation.IConversationListener;
import fr.aliasource.webmail.common.conversation.IConversationListenerFactory;

/**
 * Creates listeners responsible of conversations full text indexing
 * 
 * @author tom
 *
 */
public class ConversationListenerFactory implements IConversationListenerFactory, ICrawlerFactory {

	public ConversationListenerFactory() {
	}

	@Override
	public IConversationListener createListener(IAccount account) {
		return IndexingActivator.getDefault().createListener(account);
	}

	@Override
	public AbstractCrawler create(IIndexingParameters parameters) {
		return IndexingActivator.getDefault().create(parameters);
	}


}
