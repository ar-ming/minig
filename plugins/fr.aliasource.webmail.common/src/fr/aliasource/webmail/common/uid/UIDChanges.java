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

package fr.aliasource.webmail.common.uid;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class UIDChanges {

	private Long[] added;
	private Long[] removed;
	private Set<Long> full;
	
	private static Long[] getAdded(Set<Long> full) {
		return full.toArray(new Long[full.size()]);
	}
	
	public UIDChanges(Long[] added, Long[] removed, Set<Long> full) {
		super();
		this.added = added;
		this.removed = removed;
		this.full = full;
	}

	public UIDChanges(Set<Long> full) {
		this(getAdded(full), new Long[0], full);
	}

	public List<Long> getAdded() {
		return Arrays.asList(added);
	}

	public List<Long> getRemoved() {
		return Arrays.asList(removed);
	}

	public Set<Long> getFull() {
		return full;
	}
	
}
