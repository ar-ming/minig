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

package fr.aliasource.index.core;

import java.util.Map;

/**
 * Search result, obtained from {@link ISearchable} implementations.
 * 
 * @author tom
 * 
 */
public class Hit {

	private Map<String, Object> payload;
	private String type;

	public Hit(Map<String, Object> payload, String type) {
		super();
		this.payload = payload;
		this.type = type;
	}

	public Map<String, Object> getPayload() {
		return payload;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
