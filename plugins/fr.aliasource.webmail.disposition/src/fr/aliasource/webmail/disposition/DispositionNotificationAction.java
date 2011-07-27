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

package fr.aliasource.webmail.disposition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.disposition.storage.DispositionStorage;
import fr.aliasource.webmail.disposition.storage.DispositionStorageCache;
import fr.aliasource.webmail.proxy.IControlledAction;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class DispositionNotificationAction implements IControlledAction {

	private Log logger;

	public DispositionNotificationAction() {
		this.logger = LogFactory.getLog(getClass());
	}
	
	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		DispositionStorage dispositionStorage = new DispositionStorageCache(p.getAccount());		
		MessageId messageId = getMessageId(req);
		boolean accept = Boolean.valueOf(req.getParameter("accept"));
		if (accept) {
			IFolder folder = getFolder(p, req);
			MailMessage message = p.getAccount().getLoadMessages().load(folder, messageId);
			logger.info("send notification for mid : " + messageId.getImapId());
			dispositionStorage.notificationSent(message);
		} else {
			logger.info("decline notification for mid : " + messageId.getImapId());
			dispositionStorage.notificationDenied(messageId);
		}
	}

	private MessageId getMessageId(IParameterSource req) {
		long messageId = Long.valueOf(req.getParameter("messageId"));
		return new MessageId(messageId);
	}

	private IFolder getFolder(IProxy p, IParameterSource req) {
		String folderName = req.getParameter("folder");
		return p.getAccount().getCache().getSubscribedFolderCache().getFolderByName(folderName);
	}

	@Override
	public String getUriMapping() {
		return "/dispositionNotification.do";
	}

	@Override
	public void init(ProxyConfiguration pcf) {
	}

}
