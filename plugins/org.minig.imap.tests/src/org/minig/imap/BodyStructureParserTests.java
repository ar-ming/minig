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

package org.minig.imap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import junit.framework.TestCase;

import org.minig.imap.mime.BodyParam;
import org.minig.imap.mime.MimePart;
import org.minig.imap.mime.MimeTree;
import org.minig.imap.mime.impl.BodyStructureParser;

import fr.aliasource.utils.FileUtils;

public class BodyStructureParserTests extends TestCase {

	public byte[] openTestStructure(String filePath) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				filePath);
		try {
			return FileUtils.streamBytes(is, true);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Cannot open " + filePath);
		}
		return null;
	}

	public void testParseRFC2231EncodedParam() {
		byte[] bs = openTestStructure("rfc2231-param-encoding.dat");
		BodyStructureParser bsp = new BodyStructureParser();
		MimeTree mt = bsp.parse(bs);
		assertNotNull(mt);
		MimePart part = mt.getChildren().get(1).getChildren().get(1).getChildren().get(1);
		if (part.getAddress().equals("2.2.2")) {
			assertEquals("Infos erronées du 010910_1.pdf", part.getBodyParam("name").getValue());
			return;
		}
		fail("expected pdf not found");
	}
	
	public void testParseMozDeleted() {
		byte[] bs = openTestStructure("data/bs_01.dat");
		BodyStructureParser bsp = new BodyStructureParser();
		try {
			MimeTree mt = bsp.parse(bs);
			assertNotNull(mt);
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testParseBS04() {
		byte[] bs = openTestStructure("data/bs_04.dat");
		BodyStructureParser bsp = new BodyStructureParser();
		MimeTree mt = bsp.parse(bs);
		assertNotNull(mt);
		System.out.println("mt:\n" + mt);
	}

	public void testParseBS05() {
		byte[] bs = openTestStructure("data/bs_05.dat");
		BodyStructureParser bsp = new BodyStructureParser();
		try {
			MimeTree mt = bsp.parse(bs);
			assertNotNull(mt);
			System.out.println("mt:\n" + mt);
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testParseBS06() {
		byte[] bs = openTestStructure("data/bs_06.dat");
		BodyStructureParser bsp = new BodyStructureParser();
		MimeTree mt = bsp.parse(bs);
		assertNotNull(mt);
		System.out.println("mt:\n" + mt);
		for (MimePart mp : mt) {
			System.err.println("mp " + mp.getAddress());
			Collection<BodyParam> bp = mp.getBodyParams();
			if (bp != null) {
				for (BodyParam s : bp) {
					System.err.println(s);
				}
			}
		}
	}

	public void testInfinitLoop() {
		byte[] bs = openTestStructure("data/bs_02.dat");
		BodyStructureParser bsp = new BodyStructureParser();
		try {
			MimeTree mt = bsp.parse(bs);
			assertNotNull(mt);
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testNestedWithDominoDisclaimer() {
		byte[] bs = openTestStructure("data/bs_03.dat");
		BodyStructureParser bsp = new BodyStructureParser();
		try {
			MimeTree mt = bsp.parse(bs);
			assertNotNull(mt);
			System.out.println("parsed tree: " + mt);
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

}
