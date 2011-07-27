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

package org.minig.imap.command;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.MessageSet;
import org.minig.imap.mime.MimeTree;
import org.minig.imap.mime.impl.AtomHelper;
import org.minig.imap.mime.impl.BodyStructureParser;

public class UIDFetchBodyStructureCommand extends Command<Collection<MimeTree>> {

	private TreeSet<Long> uids;

	public UIDFetchBodyStructureCommand(Collection<Long> uid) {
		this.uids = new TreeSet<Long>(uid);
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "UID FETCH " + MessageSet.asString(uids)
				+ " (UID BODYSTRUCTURE)";
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		if (logger.isDebugEnabled()) {
			for (IMAPResponse r : rs) {
				logger.debug("ri: " + r.getPayload() + " [stream:"
						+ (r.getStreamData() != null) + "]");
			}
		}
		IMAPResponse ok = rs.get(rs.size() - 1);
		if (ok.isOk()) {
			List<MimeTree> mts = new LinkedList<MimeTree>();
			Iterator<IMAPResponse> it = rs.iterator();
			int len = rs.size() - 1;
			for (int i = 0; i < len; i++) {
				IMAPResponse ir = it.next();
				String s = ir.getPayload();

				int bsIdx = s.indexOf(" BODYSTRUCTURE ");
				if (bsIdx == -1) {
					continue;
				}

				String bs = s.substring(bsIdx + " BODYSTRUCTURE ".length());

				if (bs.length() < 2) {
					logger.warn("strange bs response: " + s);
					continue;
				}
				if (bs.charAt(1) == '(') {
					bs = bs.substring(1);
				}
				int uidIdx = s.indexOf("(UID ");
				long uid = Long.parseLong(s.substring(
						uidIdx + "(UID ".length(), bsIdx));

				byte[] bsData = AtomHelper.getFullResponse(bs, ir.getStreamData());
				BodyStructureParser bsp = new BodyStructureParser();
				try {
					MimeTree tree = bsp.parse(bsData);
					tree.setUid(uid);
					mts.add(tree);
				} catch (RuntimeException re) {
					logger.error("error parsing:\n" + new String(bsData));
					logger.error("payload was:\n" + s);
					throw re;
				}
			}
			data = mts;
		} else {
			logger.warn("bodystructure failed : " + ok.getPayload());
			data = Collections.emptyList();
		}
	}

}
