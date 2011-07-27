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

package fr.aliasource.webmail.common;

import fr.aliasource.index.core.SearchDirector;
import fr.aliasource.webmail.common.cache.AccountCache;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.message.AttachmentManager;

/**
 * Interface for mail account access
 * 
 * @author tom
 * 
 */
public interface IAccount {

	/**
	 * Provides access to cached data.
	 * 
	 * @return
	 */
	public AccountCache getCache();

	/**
	 * Returns a connection to the IMAP store
	 * 
	 * @return
	 */
	public IStoreConnection getStoreProtocol();

	public String getTransportHost();

	/**
	 * @return an object suitable for fetching conversations lists
	 */
	public IListConversations getListConversations();

	public String getUserId();

	public String getDomain();

	public String getEmailAddress();
	
	public String getUserPassword();

	/**
	 * Stops all background processes and close the imap connection
	 */
	public void close();

	/**
	 * @return an object suitable for finding conversation references
	 */
	public IFindReference getFindReference();

	/**
	 * @return an object suitable for loading imap messages
	 */
	public ILoadMessages getLoadMessages();

	/**
	 * @return an object suitable for message storage manipulation
	 */
	public IStoreMessage getStoreMessage();

	/**
	 * @return a command object usable for fetching the summary
	 */
	public AttachmentManager getAttachementManager();

	/**
	 * @return a command object suitable for setting imap flags
	 */
	public ISetFlags getFlagsCommand();

	public IMoveConversation getMoveConversation();
	
	public IMoveMessage getMoveMessage();

	public void setMailboxDelimiter(String delimiter);

	public String getMailboxDelimiter();

	public void addCloseListener(ICloseListener icl);

	public SearchDirector getSearchDirector();

	public IFolder getFolder(String conversationId);

	public Credentials getCredentials();

	public FilterStore getFilterStore();

	public void setClientChannel(IClientChannel pushHandler);

}