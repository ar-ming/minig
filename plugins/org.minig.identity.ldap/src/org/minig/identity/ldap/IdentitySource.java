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

package org.minig.identity.ldap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.backend.identity.IIdentitySource;
import org.minig.backend.identity.Identity;

import fr.aliasource.utils.LdapUtils;

public class IdentitySource implements IIdentitySource {

	private DirContext ctx;
	private String filter;
	private String baseDn;

	private static final Log logger = LogFactory.getLog(IdentitySource.class);

	public IdentitySource(DirContext ctx, String baseDn, String ldapFilter) {
		this.ctx = ctx;
		this.baseDn = baseDn;
		this.filter = ldapFilter;
	}

	@Override
	public List<Identity> getIdentities() {
		LinkedList<Identity> ret = new LinkedList<Identity>();
		LdapUtils u = new LdapUtils(ctx, baseDn);
		try {
			List<Map<String, List<String>>> attrs = u.getAttributes(filter, "",
					new String[] { "sn", "givenName", "mail" });
			for (Map<String, List<String>> m : attrs) {
				String sn = m.get("sn").get(0);
				String givenName = "";
				if (m.get("givenName") != null) {
					givenName = m.get("givenName").get(0);
				}

				StringBuilder sb = new StringBuilder();
				if (sn != null) {
					sb.append(sn);
					if (givenName != null) {
						sb.append(' ');
						sb.append(givenName);
					}
				} else if (givenName != null) {
					sb.append(givenName);
				}
				String display = sb.toString();
				List<String> mails = m.get("mail");
				for (String mail : mails) {
					ret.add(new Identity(display, mail));
				}
			}
		} catch (NamingException e) {
			logger.error(e.getMessage(), e);
		}
		return ret;
	}

	@Override
	public void release() {
		try {
			ctx.close();
		} catch (NamingException e) {
		}
	}

}
