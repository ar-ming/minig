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

package fr.aliasource.webmail.common.conversation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.aliasource.webmail.common.Activator;
import fr.aliasource.webmail.common.message.AttachmentManager;

/**
 * Ensures that we allways send body as text/html & text/plain to frontend.
 * 
 * @author tom
 * 
 */
public class BodyFormattingRegistry {

	private MailBody body;
	private AttachmentManager attachmentManager;
	private Map<String, String> attachments;

	public BodyFormattingRegistry(MailBody mb) {
		this.body = mb;
		this.attachmentManager = null;
	}

	public BodyFormattingRegistry(MailMessage mm,
			AttachmentManager attachmentManager, Map<String, String> attachments) {
		this.body = mm.getBody();
		this.attachmentManager = attachmentManager;
		this.attachments = attachments;
	}

	public void format() {
		Set<String> currentFormats = new HashSet<String>();
		currentFormats.addAll(body.availableFormats());
		List<IBodyFormatter> formatters = Activator.getDefault()
				.getFormatters();
		for (IBodyFormatter bf : formatters) {
			for (String format : currentFormats) {
				if (bf.canConvert(format)) {
					bf.addAlternateFormat(body, attachmentManager, attachments);
				}
			}
		}
	}

}
