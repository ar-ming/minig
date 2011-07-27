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

package fr.aliasource.webmail.ldap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.utils.LdapUtils;
import fr.aliasource.webmail.book.MinigContact;
import fr.aliasource.webmail.book.ContactGroup;
import fr.aliasource.webmail.book.IBookSource;
import fr.aliasource.webmail.book.MinigEmail;
import fr.aliasource.webmail.proxy.ProxyConfiguration;

public class BookSource implements IBookSource {

	private Configuration conf;
	private Log logger;

	public BookSource() {
		this.logger = LogFactory.getLog(getClass());
	}

	@Override
	public int count(String userId, String userPassword) {
		return findAll(userId, userPassword).size();
	}

	@Override
	public List<MinigContact> findAll(String userId, String userPassword) {
		return find(userId, userPassword, "", Integer.MAX_VALUE);
	}

	@Override
	public List<MinigContact> find(String userId, String userPassword,
			String query, int limit) {
		DirContext ctx = null;
		List<MinigContact> ret = new LinkedList<MinigContact>();
		String domain = "";
		int idx = userId.indexOf("@");
		if (idx > 0) {
			domain = userId.substring(idx + 1);
		}
		try {
			ctx = conf.getConnection();

			LdapUtils u = new LdapUtils(ctx, conf.getBaseDn().replace("%d",
					domain));
			List<Map<String, List<String>>> l = u.getAttributes(conf
					.getFilter(), query, new String[] { "cn", "sn",
					"givenName", "mail" });
			l = l.subList(0, Math.min(limit, l.size()));
			for (Map<String, List<String>> m : l) {
				String sn = uniqueAttribute("sn", m);
				String givenName = uniqueAttribute("givenName", m);
				String cn = uniqueAttribute("cn", m);
				if (sn.length() == 0 || givenName.length() == 0) {
					sn = cn;
					givenName = "";
				}
				MinigContact c = new MinigContact(sn, givenName, "", "", "",
						"", "", "", "");
				List<String> mails = m.get("mail");
				String lblRoot = "INTERNET;X-OBM-Ref";
				int i = 1;
				for (String mail : mails) {
					c.addEmail(lblRoot + (i++), new MinigEmail(mail));
				}

				ret.add(c);
			}
		} catch (NamingException e) {
			logger.error("findAll error", e);
		} finally {
			conf.cleanup(ctx);
		}
		return ret;
	}

	private String uniqueAttribute(String string, Map<String, List<String>> m) {
		List<String> cnl = m.get(string);
		if (cnl == null || cnl.size() == 0) {
			return "";
		} else {
			return cnl.get(0);
		}
	}

	@Override
	public ContactGroup getProvidedGroup() {
		return new ContactGroup("obm_ldap", "Users");
	}

	@Override
	public SourceType getType() {
		return SourceType.READ_ONLY;
	}

	@Override
	public void init(ProxyConfiguration pc) {
		this.conf = new Configuration(pc);
		logger.info("ldap book source initialised.");
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void insert(String userId, String userPassword, List<MinigContact> c) {
	}

}
