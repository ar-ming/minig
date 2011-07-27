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

package org.minig.listener.preview;

import java.io.IOException;
import java.io.InputStream;

import fr.aliasource.utils.FileUtils;

import junit.framework.TestCase;

public class ExtractorTests extends TestCase {

	public void testParse() throws IOException {
		String s = openMail("data/withQuote.txt");
		PreviewExtractor pe = new PreviewExtractor();
		String prev = pe.extract(s);
		assertNotNull(prev);
	}

	private String openMail(String string) throws IOException {
		InputStream is = getClass().getClassLoader()
				.getResourceAsStream(string);
		return FileUtils.streamString(is, true);
	}

}
