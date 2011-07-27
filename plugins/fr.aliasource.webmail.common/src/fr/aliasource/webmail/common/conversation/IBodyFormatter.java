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

import java.util.Map;

import fr.aliasource.webmail.common.message.AttachmentManager;

/**
 * This interface allows computing alternate representation of an email body
 * 
 * @author tom
 * 
 */
public interface IBodyFormatter {

	boolean canConvert(String mime);

	/**
	 * Computes alternate format(s) for the body. The {@link AttachmentManager}
	 * can be used for html images provided as attachment in messages.
	 * 
	 * @param mb
	 * @param am
	 *            (can be null)
	 */
	void addAlternateFormat(MailBody mb, AttachmentManager am, Map<String, String> attachments);

}
