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

package org.minig.imap.mime;

/**
 * Represents the mime tree of a message. The tree of a message can be obtained
 * by parsing the BODYSTRUCTURE response from the IMAP server.
 * 
 * @author tom
 * 
 */
public class MimeTree extends MimePart implements Iterable<MimePart> {

	private long uid;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		printTree(sb, 0, this);
		return sb.toString();
	}

	private void printTree(StringBuilder sb, int depth, MimePart mimeTree) {
		for (int i = 0; i < depth; i++) {
			sb.append("  ");
		}
		if (depth == 0) {
			sb.append("Root[" + uid + "]\n");
		} else {
			sb.append("* " + mimeTree.getAddress() + " "
					+ mimeTree.getMimeType() + "/" + mimeTree.getMimeSubtype());
			if (mimeTree.getBodyParams() != null) {
				sb.append(" " + mimeTree.getBodyParam("name"));
			}
			sb.append("\n");
		}
		for (MimePart mp : mimeTree.children) {
			printTree(sb, depth + 1, mp);
		}
	}

	public boolean isSinglePartMessage() {
		return children.size() == 1 && children.get(0).children.isEmpty();
	}

	public boolean hasAttachments() {
		for (MimePart mp : this) {
			String full = mp.getFullMimeType();
			if (!("text/plain".equals(full)) && !("text/html".equals(full))) {
				return true;
			}
		}
		return false;
	}

	public boolean hasInvitation() {
		for (MimePart mp : this) {
			if (mp.isInvitation()) {
				return true;
			}
		}
		return false;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}
}
