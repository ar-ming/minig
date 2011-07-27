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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
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
public class UploadAttachmentAction extends AbstractControlledAction {

	private Log logger = LogFactory.getLog(getClass());

	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String filename = req.getParameter("filename");
		String id = req.getParameter("id");
		String size = req.getParameter("size");
		String mime = req.getParameter("mime");

		IAccount account = p.getAccount();
		AttachmentManager mgr = account.getAttachementManager();

		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put(AttachmentManager.META_FILENAME, filename);
		metadata.put(AttachmentManager.META_SIZE, size);
		metadata.put(AttachmentManager.META_MIME, mime);
		metadata.put(AttachmentManager.META_CONTENT_ID, "");
		
		Base64 decoder = new Base64();
		String contentB64 = req.getParameter("content");
		byte[] decoded = decoder.decode(contentB64.getBytes());

		try {
			mgr.store(id, metadata, new ByteArrayInputStream(decoded));
			logger.info("Attachment " + id + ": " + filename + " " + size + " "
					+ mime + " uploaded.");
		} catch (Exception e) {
			logger.error("Error storing attachment on server", e);
		}
	}

	public String getUriMapping() {
		return "/uploadAttachment.do";
	}

}
