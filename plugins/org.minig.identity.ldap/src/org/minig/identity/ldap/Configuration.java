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

import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.utils.IniFile;

class Configuration {

	private static final String IDENTITY_LDAP_URL = "completion.ldap.url";
	private static final String IDENTITY_LDAP_BASE = "identities.ldap.basedn";

	private String baseDn;
	private String filter;
	private Properties env;
	private Log logger;
	private boolean validConf;

	public Configuration() {
		logger = LogFactory.getLog(getClass());
		IniFile backendConf = new IniFile("/etc/minig/backend_conf.ini") {
			public String getCategory() {
				return "backend";
			}
		};
		init(backendConf);
	}

	DirContext getConnection() throws NamingException {
		try {
			return new InitialDirContext(env);
		} catch (NamingException e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	public void init(IniFile pc) {
		String url = pc.getData().get(IDENTITY_LDAP_URL);
		baseDn = pc.getData().get(IDENTITY_LDAP_BASE);
		if (baseDn == null) {
			baseDn = "dc=local";
		}
		filter = "(&(objectClass=inetOrgPerson)(mailBox=%u))";

		if (url != null && baseDn != null && filter != null) {
			validConf = true;
		}

		env = new Properties();
		env.put("java.naming.factory.initial",
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put("java.naming.provider.url", url);

		logger.info(Activator.PLUGIN_ID + " initialised, url: " + url
				+ " basedn: " + baseDn + " filter: " + filter
				+ " (valid conf: " + validConf + ")");
	}

	public String getBaseDn() {
		return baseDn;
	}

	public String getFilter() {
		return filter;
	}

	public void cleanup(DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException e) {
			}
		}
	}

}
