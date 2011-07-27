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

package fr.aliasource.webmail.attachments;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.message.AttachmentManager;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

/**
 * Stores attachments sent by frontend
 * 
 * @author tom
 * 
 */
public class DownloadAttachmentAction extends AbstractControlledAction {

	private Log logger = LogFactory.getLog(getClass());

	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String id = req.getParameter("id");

		IAccount account = p.getAccount();
		AttachmentManager mgr = account.getAttachementManager();

		try {
			responder.sendStream(new FileInputStream(mgr.open(id)));
		} catch (FileNotFoundException e) {
			logger.error("Can find attachment", e);
		}

	}

	public String getUriMapping() {
		return "/downloadAttachment.do";
	}

}
