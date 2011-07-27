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

package fr.aliasource.index.solr.tests;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;
import fr.aliasource.index.core.Hit;
import fr.aliasource.index.core.Index;
import fr.aliasource.index.solr.SolrClientFactory;

public class SolrIndexTests extends TestCase {

	private Index idx;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SolrClientFactory factory = new SolrClientFactory();
		factory.init(new FakeIndexParams());
		idx = factory.getIndex("thomas@zz.com");
	}

	@Override
	protected void tearDown() throws Exception {
		idx = null;
		super.tearDown();
	}

	public void testDoQuery() {
		List<Hit> results = idx.doQuery("test");
		assertNotNull(results);
		final long qnb = 100;
		long time = System.currentTimeMillis();
		for (int i = 0; i < qnb; i++) {
			results = idx.doQuery("test");
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Runned " + qnb + " queries in " + time
				+ "ms. Running at " + (int) (qnb / (time / 1000.0)) + " query / s");
	}
	
	public void testLinkedBlockingQueue() {
		LinkedBlockingQueue<String> q = new LinkedBlockingQueue<String>();
		q.isEmpty();
		System.out.println("q is empty :/");
	}

}
