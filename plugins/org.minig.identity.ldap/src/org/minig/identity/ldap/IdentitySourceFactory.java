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

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.backend.identity.IIdentitySource;
import org.minig.backend.identity.IIdentitySourceFactory;

public class IdentitySourceFactory implements IIdentitySourceFactory {

	private Log logger = LogFactory.getLog(getClass());
	private Configuration configuration;

	public IdentitySourceFactory() {
		this.configuration = Activator.getDefault().getConfiguration();
	}

	@Override
	public IIdentitySource getIdentitySource(String userEmail) {
		try {
			return new IdentitySource(configuration.getConnection(), configuration.getBaseDn(),
					configuration.getFilter().replace("%u", userEmail));
		} catch (NamingException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

}
