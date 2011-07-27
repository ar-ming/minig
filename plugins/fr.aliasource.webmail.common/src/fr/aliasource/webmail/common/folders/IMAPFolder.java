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

package fr.aliasource.webmail.common.folders;

/**
 * IMAP folder abstraction
 * 
 * @author tom
 *
 */
public class IMAPFolder implements IFolder, Comparable<IFolder> {

	private String displayName;
	private String name;
	private Boolean subscribed;
	private Boolean shared;
	private int id;
	
	public IMAPFolder(String name) {
		this(name, name);
	}

	public IMAPFolder(String displayName, String name) {
		this(displayName, name, null, null);	
	}
	
	
	public IMAPFolder(String displayName, String name, Boolean subscribed, Boolean shared) {
		this.displayName = displayName;
		this.name = name;
		this.subscribed = subscribed;
		this.shared = shared;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IMAPFolder)) {
			return false;
		}
		IMAPFolder f = (IMAPFolder) obj;
		return name.equalsIgnoreCase(f.name);
	}

	@Override
	public int hashCode() {
		return name.toLowerCase().hashCode();
	}

	@Override
	public int compareTo(IFolder o) {
		return name.compareToIgnoreCase(o.getName());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isSubscribed() {
		return subscribed;
	}

	public void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}

	@Override
	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}
	
}
