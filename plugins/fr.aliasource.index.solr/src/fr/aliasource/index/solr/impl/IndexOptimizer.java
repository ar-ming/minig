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

package fr.aliasource.index.solr.impl;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.index.solr.SolrClient;

public class IndexOptimizer extends TimerTask {

	private SolrClient sc;
	private static final Log logger = LogFactory.getLog(IndexOptimizer.class);

	public IndexOptimizer(SolrClient sc) {
		this.sc = sc;
	}

	@Override
	public void run() {
		try {
			long time = System.currentTimeMillis();
			sc.optimize();
			time = System.currentTimeMillis() - time;
			logger.info("index optimize took " + (time / 1000) + " seconds.");
		} catch (Throwable t) {
			logger.error("Failure on index optimize", t);
		}

	}

}
