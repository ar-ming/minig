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

package fr.aliasource.webmail.proxy.api;

public class Completion {

	private String value;
	private String displayName;
	private String compareValue;

	public Completion(String value, String displayName) {
		super();
		this.value = value;
		this.displayName = displayName;
		this.compareValue = displayName+value;
	}

	public String getValue() {
		return value;
	}

	public String getDisplayName() {
		return displayName;
	}

	
	
	public boolean equals(Object obj) {
		return compareValue.equals(((Completion)obj).compareValue);
	}

	@Override
	public int hashCode() {
		return compareValue.hashCode();
	}

	public String getCompareValue() {
		return compareValue;
	}

}
