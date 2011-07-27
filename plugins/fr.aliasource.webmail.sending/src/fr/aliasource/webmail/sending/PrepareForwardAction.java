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

package fr.aliasource.webmail.sending;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.message.AttachmentManager;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class PrepareForwardAction extends AbstractControlledAction {

	private Log logger = LogFactory.getLog(getClass());

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String fName = req.getParameter("folder");
		String[] uids = req.getParameter("uids").split(",");

		MailMessage mm = new MailMessage();

		AttachmentManager atMgr = p.getAccount().getAttachementManager();
		Map<String, String> attachments = new HashMap<String, String>();
		IStoreConnection con = p.getAccount().getStoreProtocol();
		try {
			con.select(fName);
			int i = 0;
			for (String uidString : uids) {
				long uid = Long.parseLong(uidString);
				logger.info("prepare forward for uid " + uid);
				InputStream in = con.uidFetchMessage(uid);
				String id = atMgr.allocateAttachementId();
				Map<String, String> meta = new HashMap<String, String>();
				String name = "forwarded_message_" + (i++) + ".eml";
				meta.put(AttachmentManager.META_FILENAME, name);
				meta.put(AttachmentManager.META_MIME, "message/rfc822");
				atMgr.store(id, meta, in);
				attachments.put(id, name);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			con.destroy();
		}
		mm.setAttachements(attachments);
		mm.setSubject("forward");
		responder.sendMessages(new MailMessage[] { mm });
	}

	@Override
	public String getUriMapping() {
		return "/prepareForward.do";
	}

}
