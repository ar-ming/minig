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

package org.minig.cache;

import java.io.IOException;
import java.io.InputStream;

import fr.aliasource.utils.FileUtils;

public class SchemaReader {

	int cur = 0;
	private String[] queries;

	public SchemaReader(String path) {
		String s = openSql(path);
		queries = s.replace("\n", "").split(";");

	}

	public String nextQuery() {
		if (cur < queries.length) {
			return queries[cur++];
		} else {
			return null;
		}
	}

	private String openSql(String filePath) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				filePath);
		try {
			return FileUtils.streamString(is, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
