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

import fr.aliasource.webmail.common.ConversationReferenceList;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.VersionnedList;
import fr.aliasource.webmail.common.folders.IFolder;

public class ListConversationsImpl extends ProxyAction {

	public ListConversationsImpl(IAccount ac) {
		super(ac);
	}

	public ConversationReferenceList list(IFolder f, int page, int pageLength) {
		ConversationReferenceList ret = null;
		
		try {
			ret =getAccount().getListConversations().list(f, page,
					pageLength);
		} catch (Exception e) {
			logger.error("Error listing conversations", e);
			ret = new ConversationReferenceList(new VersionnedList<ConversationReference>(), 0);
		}
		return ret;
	}

}
