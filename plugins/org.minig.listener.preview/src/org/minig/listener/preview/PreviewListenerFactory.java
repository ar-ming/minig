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

package org.minig.listener.preview;

import java.util.HashSet;
import java.util.Set;

import fr.aliasource.webmail.common.AccountConfiguration;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.conversation.IConversationListener;
import fr.aliasource.webmail.common.conversation.IConversationListenerFactory;

/**
 * Creates listeners responsible of computing preview of conversations.
 * 
 * @author tom
 * 
 */
public class PreviewListenerFactory implements IConversationListenerFactory {

	public PreviewListenerFactory() {
	}

	@Override
	public IConversationListener createListener(IAccount account) {
		Set<String> skipped = loadSkippedFolders(account);
		return new PreviewListener(account, skipped);
	}

	private Set<String> loadSkippedFolders(IAccount account) {
		HashSet<String> skippedFolders = new HashSet<String>();
		AccountConfiguration ac = new AccountConfiguration();
		String[] skipped = ac.getSetting(account.getUserId(),
				AccountConfiguration.SKIPPED_FOLDERS).split(",");
		for (String s : skipped) {
			skippedFolders.add(s.trim().toLowerCase().replace("%d",
					account.getMailboxDelimiter()));
		}
		return skippedFolders;
	}

}
