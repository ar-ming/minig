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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.SortedMap;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import fr.aliasource.webmail.common.ConversationReferenceList;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.folders.IFolder;

/**
 * Crafts replies to clients (the View in the proxy MVC architecture)
 * 
 * @author tom
 *
 */
public interface IResponder {

	/**
	 * Sends Access token to client after successfull login
	 * @param token the generated token
	 */
	void sendToken(String token);
	
	/**
	 * Sends a list of folders
	 * 
	 * @param folders
	 */
	void sendFolderList(List<IFolder> folders);

	/**
	 * Respond with 403 FORBIDDEN to client
	 */
	void denyAccess(String cause);

	void sendConversationsPage(ConversationReferenceList listConversations);

	void sendConversation(ConversationReference findConversation);

	void sendMessages(MailMessage[] fetchMessages);

	void sendMessages(ConversationReference cr, MailMessage[] fetchMessages);

	void sendCompletions(List<Completion> possibleCompletions);

	void sendConversationIds(String[] convIds);

	void sendSummary(SortedMap<IFolder, Integer> summary);
	
	/**
	 * Low level sending method
	 * 
	 * @param doc
	 * @throws TransformerException
	 * @throws IOException
	 */
	void sendDom(Document doc) throws TransformerException, IOException;

	void sendStream(InputStream in);

	void sendError(String string);

	void sendString(String string);
	
	/**
	 * Will return a 304 http error to the caller
	 */
	void sendNothingChanged();

}