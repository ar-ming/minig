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

package fr.aliasource.webmail.proxy.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import fr.aliasource.webmail.common.ConversationReferenceList;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.message.SendParameters;

/**
 * Core MiniG backend interface
 * 
 * @author tom
 * 
 */
public interface IProxy extends IStoppable {

	boolean doLogin(String login, String domain, String password);

	String getToken();

	void setToken(String token);

	ConversationReferenceList listConversations(IFolder folder, int page,
			int pageLength);

	ConversationReference findConversation(String convId);

	MailMessage[] fetchMessages(IFolder f, List<MessageId> mids, boolean truncate);
	
	List<Completion> getPossibleCompletions(String type, String query, int limit);

	boolean checkPassword(String password);

	void addClientReference();

	int releaseClientReference();
	
	/**
	 * Store a mail message
	 * 
	 * @param f
	 * @param mm
	 * @return conversation id
	 */
	String store(IFolder f, MailMessage mm, SendParameters sp);

	/**
	 * This is useful for plug-in controlled actions.
	 * 
	 * @return
	 */
	IAccount getAccount();

	void setFlags(Set<String> convIds, String set);

	void unsetFlags(Set<String> convIds, String unset);

	void setFlags(String query, String set);

	void unsetFlags(String query, String unset);

	Set<String> moveConversation(IFolder dest, Set<String> convIds);

	Set<String> moveConversation(String query, IFolder dest);

	Set<String> copy(IFolder dest, Set<String> convIds);

	Set<String> copy(String query, IFolder dest);

	/**
	 * Allocates a unique identifier suitable for uploading an attachment.
	 * 
	 * @return
	 */
	String allocateAttachmentId();

	IFolderService getFolderService();

	ISettingService getSettingService();

	Set<String> moveMessage(IFolder dest, String convId, Collection<Long> messageIds);

}
