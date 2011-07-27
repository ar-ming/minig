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

import junit.framework.TestCase;
import fr.aliasource.index.core.Index;
import fr.aliasource.index.solr.SolrClientFactory;

public class SolrIndexFactoryTests extends TestCase {

	public void testCtor() {
		SolrClientFactory factory = new SolrClientFactory();
		assertNotNull(factory);
	}
	
	public void testInit() {
		SolrClientFactory factory = new SolrClientFactory();
		factory.init(new FakeIndexParams());
	}
	
	public void testGetIndex() {
		SolrClientFactory factory = new SolrClientFactory();
		factory.init(new FakeIndexParams());
		Index idx = factory.getIndex("thomas@zz.com");
		assertNotNull(idx);
	}
}
