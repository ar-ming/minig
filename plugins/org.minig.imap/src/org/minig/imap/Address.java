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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.utils.StringUtils;

/**
 * Simple mail address representation
 * 
 * @author tom
 * 
 */
public final class Address {

	private String mail;
	private String displayName;

	private static final Log logger = LogFactory.getLog(Address.class);

	public Address(String mail) {
		this(null, mail);
	}

	public Address(String displayName, String mail) {
		if (displayName != null) {
			this.displayName = StringUtils
					.stripAddressForbiddenChars(displayName);
		}
		if (mail != null && mail.contains("@")) {
			this.mail = StringUtils.stripAddressForbiddenChars(mail);
		} else {
			// FIXME ...
			if (logger.isDebugEnabled()) {
				logger
						.debug("mail: "
								+ mail
								+ " is not a valid email, building a john.doe@minig.org");
			}
			this.displayName = StringUtils.stripAddressForbiddenChars(mail);
			this.mail = "john.doe@minig.org";
		}
	}

	public String getMail() {
		return mail;
	}

	public String getDisplayName() {
		return displayName != null ? displayName : mail;
	}

	@Override
	public boolean equals(Object obj) {
		return mail.equals(((Address) obj).mail);
	}

	@Override
	public String toString() {
		return "" + displayName + " <" + mail + ">";
	}

	@Override
	public int hashCode() {
		return mail.hashCode();
	}

}
