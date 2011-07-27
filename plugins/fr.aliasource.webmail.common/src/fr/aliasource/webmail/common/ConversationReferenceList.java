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

import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.PendingNotifications;
import fr.aliasource.webmail.common.conversation.VersionnedList;

public class ConversationReferenceList {

	private int fullLength;
	private VersionnedList<ConversationReference> page;
	private PendingNotifications pendingNotifications;

	public ConversationReferenceList(VersionnedList<ConversationReference> page,
			int fullLength) {
		this.page = page;
		this.fullLength = fullLength;
	}

	public int getFullLength() {
		return fullLength;
	}

	public VersionnedList<ConversationReference> getPage() {
		return page;
	}

	public PendingNotifications getPendingNotifications() {
		return pendingNotifications;
	}

	public void setPendingNotifications(PendingNotifications notifs) {
		this.pendingNotifications = notifs;
	}

}
