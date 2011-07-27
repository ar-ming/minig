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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.common.ConversationReferenceList;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.LocatorRegistry;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.message.SendParameters;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.Completion;
import fr.aliasource.webmail.proxy.api.IFolderService;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.ISettingService;

/**
 * MiniG server side process implementation
 * 
 * @author tom
 * 
 */
public class ProxyImpl implements IProxy {

	private LoginImpl loginService;
	private IFolderService folderService;
	private ISettingService settingService;
	private IAccount account;
	private String token;
	private ListConversationsImpl lconvService;
	private ShowConversationImpl showConvService;
	private CompletionRegistry completionRegistry;
	private Log logger = LogFactory.getLog(getClass());
	private String password;

	private AtomicInteger clientReferences;

	public ProxyImpl(ProxyConfiguration conf,
			CompletionRegistry completionRegistry, LocatorRegistry locator) {
		this.completionRegistry = completionRegistry;
		loginService = new LoginImpl(conf, locator);
		clientReferences = new AtomicInteger(0);
	}

	public boolean doLogin(String login, String domain, String password) {
		boolean valid = loginService.doLogin(login, domain, password);
		if (valid) {
			account = loginService.getAccount();
			initSecureServices();
			this.password = password;
			clientReferences.incrementAndGet();
		}
		return valid;
	}

	public boolean checkPassword(String password) {
		return this.password.equals(password);
	}

	public void addClientReference() {
		clientReferences.incrementAndGet();
	}

	public int releaseClientReference() {
		return clientReferences.decrementAndGet();
	}

	private void initSecureServices() {
		folderService = new FolderServiceImpl(account);
		settingService = new SettingServiceImpl(account);
		lconvService = new ListConversationsImpl(account);
		showConvService = new ShowConversationImpl(account);
	}

	public void stop() {
		account.close();
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public ConversationReferenceList listConversations(IFolder folder,
			int page, int pageLength) {
		return lconvService.list(folder, page, pageLength);
	}

	public MailMessage[] fetchMessages(IFolder f, List<MessageId> mids, boolean truncate) {
		return showConvService.fetchMessages(f, mids, truncate);
	}

	public ConversationReference findConversation(String convId) {
		return showConvService.findConversation(convId);
	}

	public List<Completion> getPossibleCompletions(String type, String query,
			int limit) {
		return completionRegistry.complete(getAccount(), type, query, limit);
	}

	public String store(IFolder dest, MailMessage mm, SendParameters parameters) {
		return account.getStoreMessage().store(dest, mm, parameters);
	}

	public Set<String> moveConversation(IFolder dest, Set<String> convIds) {
		return account.getMoveConversation().move(dest, convIds);
	}

	public Set<String> moveConversation(String query, IFolder dest) {
		return account.getMoveConversation().move(query, dest);
	}
	
	@Override
	public Set<String> moveMessage(IFolder dest, String convId, Collection<Long> messId) {
		return account.getMoveMessage().moveMessage(dest, convId, messId);
	}

	public Set<String> copy(IFolder dest, Set<String> convIds) {
		return account.getMoveConversation().copy(dest, convIds);
	}

	public Set<String> copy(String query, IFolder dest) {
		return account.getMoveConversation().copy(query, dest);
	}

	public IAccount getAccount() {
		return account;
	}

	public void setFlags(Set<String> convIds, String flag) {
		try {
			account.getFlagsCommand().setFlags(convIds, flag);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void unsetFlags(Set<String> convIds, String flag) {
		try {
			account.getFlagsCommand().unsetFlags(convIds, flag);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void setFlags(String query, String flag) {
		try {
			account.getFlagsCommand().setFlags(query, flag);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void unsetFlags(String query, String flag) {
		try {
			account.getFlagsCommand().unsetFlags(query, flag);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public String allocateAttachmentId() {
		try {
			return getAccount().getAttachementManager().allocateAttachementId();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public IFolderService getFolderService() {
		return folderService;
	}

	public ISettingService getSettingService() {
		return settingService;
	}

}
