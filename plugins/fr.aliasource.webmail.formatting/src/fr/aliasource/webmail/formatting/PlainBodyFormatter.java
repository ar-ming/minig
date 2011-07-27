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

package fr.aliasource.webmail.formatting;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
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
 * Converts HTML mail body to text
 * 
 * @author tom
 * 
 */
public class PlainBodyFormatter implements IBodyFormatter {

	private Log logger = LogFactory.getLog(getClass());

	public PlainBodyFormatter() {
	}

	@Override
	public void addAlternateFormat(MailBody mb, AttachmentManager am,
			Map<String, String> attachments) {
		String html = mb.getValue("text/html");
		String converted = convert(html);
		mb.addConverted("text/plain", converted);
	}

	private String convert(String html) {
		String retVal = "[html message]\n";
		String ret = html;
		ret = ret.replace("<br/>", "\n");
		ret = ret.replace("<BR/>", "\n");
		ret = ret.replace("<BR>", "\n");
		ret = ret.replace("<br>", "\n");
		ret = ret.replace("</td>", " </td>");
		ret = ret.replace("</TD>", " </TD>");

		ElementRemover er = new ElementRemover() {

			@Override
			public void comment(XMLString text, Augmentations augs)
					throws XNIException {
				// strip out comments, outlook loves comments in its html
			}

		};
		er.removeElement("script");
		er.removeElement("style");
		StringWriter sw = new StringWriter();

		XMLDocumentFilter[] filters = { er, new Writer(sw, "UTF-8") };

		XMLParserConfiguration xpc = new HTMLConfiguration();
		xpc
				.setProperty("http://cyberneko.org/html/properties/filters",
						filters);
		xpc.setProperty("http://cyberneko.org/html/properties/names/elems",
				"lower");
		xpc.setFeature("http://cyberneko.org/html/features/augmentations", true);


		XMLInputSource xis = new XMLInputSource(null, null, null,
				new StringReader(ret), null);

		try {
			xpc.parse(xis);
			retVal = sw.toString();
			retVal = StringEscapeUtils.unescapeHtml(retVal);
			retVal = retVal.replace("&apos;", "'");
		} catch (Exception e) {
			logger.error(e.getMessage() + ". HTML was: \n" + html);
		}

		return retVal;
	}

	@Override
	public boolean canConvert(String mime) {
		return "text/html".equals(mime);
	}

}
