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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.minig.imap.EncodedWord;

public class SubjectEncodingTests extends TestCase {

	public void testEncoding() throws UnsupportedEncodingException {
		String input = "€ €";
		for (char c : input.toCharArray()) {
			System.out.println("char " + (int) c);
		}

		Charset utf8 = Charset.forName("UTF-8");
		System.out.println("INP " + input);
		String encoded = EncodedWord.encode(input, utf8,
				EncodedWord.QUOTED_PRINTABLE).toString();
		System.out.println("ENC " + encoded);
		
		String decoded = EncodedWord.decode(encoded).toString();
		System.out.println("DEC " + decoded);

		assertEquals(decoded, input);
		//assertFalse(decoded.equals(encoded));
	}
}
