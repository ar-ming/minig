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

package org.minig.imap.mime.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.minig.imap.mime.MimePart;

/**
 * Used to iterate leaf parts of the MIME tree
 * 
 * @author tom
 *
 */
public class LeafPartsIterator implements Iterator<MimePart> {

	private List<MimePart> l;
	private Iterator<MimePart> it;

	public LeafPartsIterator(MimePart mp) {
		l = new LinkedList<MimePart>();
		buildLeafList(mp);
		it = l.iterator();
	}

	private void buildLeafList(MimePart mp) {
		if (mp.getChildren().size() == 0) {
			l.add(mp);
		} else {
			for (MimePart m : mp.getChildren()) {
				buildLeafList(m);
			}
		}
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public MimePart next() {
		return it.next();
	}

	@Override
	public void remove() {
	}

}
