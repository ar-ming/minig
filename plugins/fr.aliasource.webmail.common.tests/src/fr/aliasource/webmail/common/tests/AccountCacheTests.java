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

package fr.aliasource.webmail.common.tests;

import java.io.File;

import fr.aliasource.webmail.common.cache.AccountCache;

public class AccountCacheTests extends WebmailTestCase {

	public void testGetCache() {
		AccountCache ac = account.getCache();
		assertNotNull(ac);
	}
	
	public void testGetPath() {
		AccountCache ac = account.getCache();
		String path = ac.getCachePath();
		System.out.println("cache path: "+path);
		assertNotNull(path);
		assertTrue(new File(path).isDirectory());
	}
	
	public void testGetCaches() {
		AccountCache ac = account.getCache();
		assertNotNull(ac.getSubscribedFolderCache());
		assertNotNull(ac.getAvailableFolderCache());
		assertNotNull(ac.getConversationCache());
		assertNotNull(ac.getCacheManager());
	}
	
	public void testStartShutdown() {
		AccountCache ac = account.getCache();
		ac.start();
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ac.shutdown();
	}
}
