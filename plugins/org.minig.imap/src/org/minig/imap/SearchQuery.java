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

package org.minig.imap;

import java.util.Date;

public class SearchQuery {

	public SearchQuery() {
		this(null);
	}

	/**
	 * 
	 * @param after
	 *            Messages whose internal date (disregarding time and timezone)
	 *            is within or later than the specified date.
	 */
	public SearchQuery(Date after) {
		this.after = after;
	}

	private Date after;

	public Date getAfter() {
		return after;
	}

	public void setAfter(Date after) {
		this.after = after;
	}

}
