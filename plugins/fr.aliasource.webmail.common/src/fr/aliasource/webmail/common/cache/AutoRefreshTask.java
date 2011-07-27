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

package fr.aliasource.webmail.common.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Periodic task used to issue refreshAll calls on the CacheManager for a
 * connected user.
 * 
 * @author tom
 * 
 */
public class AutoRefreshTask implements Runnable {

	private CacheManager mgr;
	private Log logger;
	private boolean stopped;

	public AutoRefreshTask(CacheManager mgr) {
		this.logger = LogFactory.getLog(getClass());
		this.mgr = mgr;
	}

	@Override
	public void run() {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting refresh.");
		}
		while (!stopped) {
			try {
				Thread.sleep(24 * 1000);
			} catch (InterruptedException e) {
			}
			mgr.refreshAll();
		}
		logger.info("leaving autorefesh loop.");
	}

	public void stop() {
		stopped = true;
	}

}
