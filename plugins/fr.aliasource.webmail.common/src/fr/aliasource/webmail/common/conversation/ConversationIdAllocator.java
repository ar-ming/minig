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

import java.util.concurrent.atomic.AtomicInteger;

import fr.aliasource.webmail.common.folders.IFolder;

/**
 * Allocates unique conversation ids
 * 
 * @author tom
 * 
 */
public final class ConversationIdAllocator {

	private AtomicInteger subId;
	private String fName;
	private boolean participantsAreRecipients;

	public ConversationIdAllocator(IFolder f, boolean participantsAreRecipients) {
		subId = new AtomicInteger(0);
		this.fName = f.getName();
		this.participantsAreRecipients = participantsAreRecipients;
	}

	public String allocateUnique() {
		StringBuilder sb = new StringBuilder(fName);
		sb.append('/');
		sb.append(System.currentTimeMillis());
		sb.append('-');
		sb.append(subId.incrementAndGet());
		return sb.toString();
	}

	public String getfName() {
		return fName;
	}

	public boolean isParticipantsAreRecipients() {
		return participantsAreRecipients;
	}

}
