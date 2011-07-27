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

package fr.aliasource.webmail.common.cache;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.folders.FetchSummaryCommand;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;

/**
 * This class is responsible for caching the unread count summary list
 * 
 * @author tom
 * 
 */
public class SummaryCache extends FileCache<SortedMap<IFolder, Integer>> {

	public SummaryCache(IAccount account) {
		super(account, "summary", "summary", new FetchSummaryCommand(account));
	}

	public void writeToCache(SortedMap<IFolder, Integer> summary)
			throws ParserConfigurationException, FactoryConfigurationError,
			FileNotFoundException, TransformerException {
		Document dom = DOMUtils.createDoc(null, "summary");

		for (IFolder f : summary.keySet()) {
			Element e = DOMUtils.createElement(dom.getDocumentElement(),
					"folder");
			e.setAttribute("name", f.getName());
			e.setAttribute("displayName", f.getDisplayName());
			e.setAttribute("unread", summary.get(f).toString());
		}
		DOMUtils.serialise(dom, new FileOutputStream(getCacheFile()));
	}

	protected SortedMap<IFolder, Integer> loadCacheFromDOM(Document sub) {
		SortedMap<IFolder, Integer> summary = new TreeMap<IFolder, Integer>();

		if (sub != null) {
			NodeList nodes = sub.getElementsByTagName("folder");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element e = (Element) nodes.item(i);
				IMAPFolder folder = new IMAPFolder(e.getAttribute("displayName"), e.getAttribute("name"));
				summary.put(folder, new Integer(e.getAttribute("unread")));
			}
		}

		return summary;
	}

}
