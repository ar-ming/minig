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

package fr.aliasource.webmail.formatting.htmlcleaner;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.filters.ElementRemover;
import org.cyberneko.html.filters.Writer;

import fr.aliasource.webmail.common.conversation.IBodyFormatter;
import fr.aliasource.webmail.common.conversation.MailBody;
import fr.aliasource.webmail.common.message.AttachmentManager;

/**
 * Clean HTML mail body to HTML without images & js
 * 
 * @author david
 * 
 */
public abstract class AbstractHTMLCleaner implements IBodyFormatter {

	private Log logger = LogFactory.getLog(getClass());

	protected Set<String> removedElements;
	protected Set<String> removedAttributes;

	private String resultMime;

	protected AbstractHTMLCleaner(String resultMime) {
		this.resultMime = resultMime;
		removedElements = new HashSet<String>();
		removedElements.add("script");
		removedElements.add("style");

		removedAttributes = new HashSet<String>();
		removedAttributes.add("onload");
		removedAttributes.add("onclick");
		removedAttributes.add("onsubmit");
		removedAttributes.add("target");
		removedAttributes.add("id");
	}

	@Override
	public void addAlternateFormat(MailBody mb, AttachmentManager am,
			Map<String, String> attachments) {
		String html = mb.getValue("text/html");
		String converted = convert(html, am, attachments);
		mb.addConverted(resultMime, converted);
	}

	private String convert(String html, AttachmentManager am,
			Map<String, String> attachments) {

		String cleanHtml = html;

		ElementRemover er = new ElementRemover();

		Attribute[] attr = HTML.getAllAttributeKeys();
		Set<String> allowedAttributes = new HashSet<String>();
		for (int i = 0; i < attr.length; i++) {
			if (!removedAttributes.contains(attr[i].toString().toLowerCase())) {
				allowedAttributes.add(attr[i].toString());
			}
		}

		Tag[] tags = HTML.getAllTags();
		for (int i = 0; i < tags.length; i++) {
			if (!removedElements.contains(tags[i].toString())) {
				er.acceptElement(tags[i].toString(), allowedAttributes.toArray(new String[allowedAttributes
						.size()]));
			}
		}

		for (String s : removedElements) {
			er.removeElement(s);
		}

		StringWriter sw = new StringWriter();

		XMLDocumentFilter[] filters = { er, new Writer(sw, "UTF-8") };

		XMLParserConfiguration xpc = new HTMLConfiguration();
		xpc
				.setProperty("http://cyberneko.org/html/properties/filters",
						filters);
		xpc.setProperty("http://cyberneko.org/html/properties/names/elems",
				"lower");

		XMLInputSource xis = new XMLInputSource(null, null, null,
				new StringReader(html), null);

		try {
			xpc.parse(xis);
			cleanHtml = sw.toString();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		// all links open in other tab
		cleanHtml = cleanHtml.replace("<a ", "<a target=\"_blank\" ");
		
		// Replace cid:images by attachment url
		if (attachments != null) {
			for (String aId : attachments.keySet()) {
				Map<String, String> meta = am.getMetadata(aId);
				if (meta.get("content-id") != null) {
					String cid = "cid:"
							+ meta.get("content-id").substring(1,
									meta.get("content-id").length() - 1);
					cleanHtml = cleanHtml.replace(cid,
							"download/" + aId + "/" + meta.get("filename"));
				}
			}
		}

		return cleanHtml;
	}

	@Override
	public boolean canConvert(String mime) {
		return "text/html".equals(mime);
	}

}
