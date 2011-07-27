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

package org.minig.formatting.tests;

import fr.aliasource.webmail.common.conversation.MailBody;
import fr.aliasource.webmail.formatting.PlainBodyFormatter;
import junit.framework.TestCase;

public class PlainBodyFormatterTests extends TestCase {

	public void testEntities() {
		String html = "<html><body><h1>Hello &eacute;ric</h1></body></html>";
		MailBody mb = new MailBody();
		mb.addConverted("text/html", html);

		PlainBodyFormatter plb = new PlainBodyFormatter();
		plb.addAlternateFormat(mb, null, null);
		String plain = mb.getValue("text/plain");

		System.out.println("text/plain is:\n" + plain);
		assertFalse(plain.contains("acute"));
	}

	public void testComments() {
		String html = "<html><body><h1>Hello comment <!-- skipped --></h1></body></html>";
		MailBody mb = new MailBody();
		mb.addConverted("text/html", html);

		PlainBodyFormatter plb = new PlainBodyFormatter();
		plb.addAlternateFormat(mb, null, null);
		String plain = mb.getValue("text/plain");

		System.out.println("text/plain is:\n" + plain);
		assertFalse(plain.contains("skipped"));
	}
}
