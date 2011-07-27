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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PreviewExtractor {

	private static final Log logger = LogFactory.getLog(PreviewExtractor.class);

	public String extract(String plain) throws IOException {
		BufferedReader br = new BufferedReader(new StringReader(plain));
		List<String> lines = new LinkedList<String>();

		String line;
		boolean inQuote = false;
		while ((line = br.readLine()) != null) {
			if (line.trim().length() == 0) {
				continue;
			}

			if (line.startsWith(">") || line.startsWith("&gt;")) {
				if (!inQuote && !lines.isEmpty()) {
					lines.remove(lines.size() - 1); // remove the "quoting X :"
					// line
					inQuote = true;
				}
			} else {
				lines.add(line);
				inQuote = false;
			}
		}
		StringBuilder prev = new StringBuilder();
		int len = 0;
		for (String s : lines) {
			prev.append(s);
			len += s.length();
			if (len >= 200) {
				break;
			}
			prev.append(' ');
		}
		String r = prev.toString().replaceAll("\\<.*?>","");
		r = r.replaceAll("-|_", " ");
		r = r.replaceAll("  +", "").trim();

		if (logger.isDebugEnabled()) {
			logger.debug("preview: " + r);
		}
		return r;
	}

}
