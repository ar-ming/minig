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

package fr.aliasource.webmail.pool.tests;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import fr.aliasource.webmail.pool.Pool;

public class PoolTest extends TestCase {

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

	public void testPool() throws InterruptedException {
		Pool<IFake> p = new Pool<IFake>("fakePoolId", new FakeFactory(), 2,
				"destroy", 500);
		assertNotNull(p);
		System.out.println("1 p.usageCount: " + p.getUsageReport());

		sleep(2000);

		IFake f1 = p.get();

		sleep(2000);

		System.out.println("2 p.usageCount: " + p.getUsageReport());

		IFake f2 = p.get();
		assertNotNull(f2);

		sleep(2000);

		System.out.println("3 p.usageCount: " + p.getUsageReport());

		try {
			System.out.println("try: p.usageCount: " + p.getUsageReport());
			p.get(1, TimeUnit.SECONDS, false);
			fail("Should not be able to get a third item from this pool");
		} catch (Throwable t) {
			System.out.println("afterTry: p.usageCount: " + p.getUsageReport());
			// we _MUST_ get a runtime exception here as the pool only holds 2
			// connections
		}
		sleep(2000);

		f1.destroy();

		System.out.println("DESTROY p.usageCount: " + p.getUsageReport());
		sleep(2000);

		System.out.println("AA p.usageCount: " + p.getUsageReport());

		f1 = p.get();
		assertNotNull(f1);

		System.out.println("ZZ p.usageCount: " + p.getUsageReport());

		sleep(2000);

		f1.destroy();

		System.out.println("BABA p.usageCount: " + p.getUsageReport());

		sleep(2000);

		f2.destroy();
		System.out.println("BIBI p.usageCount: " + p.getUsageReport());

		System.out.println("before destroy !!");
		p.destroy();

		System.out.println("p.usageCount: " + p.getUsageReport());

	}
}
