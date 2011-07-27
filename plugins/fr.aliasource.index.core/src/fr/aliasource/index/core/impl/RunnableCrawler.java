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

package fr.aliasource.index.core.impl;

import fr.aliasource.index.core.AbstractCrawler;

/**
 * Runnable used for running a given crawler in full or incremental mode.
 * 
 * @author tom
 * 
 */
public class RunnableCrawler implements Runnable {

	private AbstractCrawler crawler;

	public RunnableCrawler(AbstractCrawler crawler) {
		this.crawler = crawler;
	}

	@Override
	public void run() {
		crawler.startFetch();
	}

}
