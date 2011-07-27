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

package fr.aliasource.webmail.proxy.impl;

import java.util.List;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IFolder;

public class ShowConversationImpl extends ProxyAction {

	public ShowConversationImpl(IAccount ac) {
		super(ac);
	}

	public ConversationReference findConversation(String convId) {
		return getAccount().getFindReference().find(convId);
	}

	public MailMessage[] fetchMessages(IFolder f, List<MessageId> mids,
			boolean truncate) {
		return getAccount().getLoadMessages().load(f, mids,truncate);
	}

}
