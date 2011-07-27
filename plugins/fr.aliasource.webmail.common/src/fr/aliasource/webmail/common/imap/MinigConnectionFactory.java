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

package fr.aliasource.webmail.common.imap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.pool.IPoolableObjectFactory;

public class MinigConnectionFactory implements
		IPoolableObjectFactory<IStoreConnection> {

	private String password;
	private String login;
	private int port;
	private String host;
	private Log logger = LogFactory.getLog(getClass());

	public MinigConnectionFactory(String host, int port, String login,
			String password) {
		this.host = host;
		this.port = port;
		this.login = login;
		this.password = password;
	}

	@Override
	public IStoreConnection createNewObject() {
		MinigConnection ret = null;
		try {
			ret = new MinigConnection(host, port, login, password);
		} catch (Exception e) {
			logger.error("Cannot establish IMAP connection for " + login
					+ " with password '" + password + "' ("
					+ e.getMessage() + ")");
		}
		return ret;
	}

}
