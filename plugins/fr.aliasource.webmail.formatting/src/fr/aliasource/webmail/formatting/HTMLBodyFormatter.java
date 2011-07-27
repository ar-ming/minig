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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import fr.aliasource.webmail.common.conversation.IBodyFormatter;
import fr.aliasource.webmail.common.conversation.MailBody;
import fr.aliasource.webmail.common.message.AttachmentManager;

/**
 * Convert text/plain mail body to HTML. The HTML version should be used for
 * display & rich formatting.
 * 
 * @author tom
 * 
 */
public class HTMLBodyFormatter implements IBodyFormatter {

	private static String PATTERN_EMAIL = "[a-zA-Z_0-9\\-.]+@[a-zA-Z_0-9\\-.]+\\.[a-z]+";

	private static String PATTERN_URL = "(((http://)|(https://)|(w{3}.))"
			+ "+[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)"
			+ "+(/[#&;\\n\\-=?:|,()@\\+\\%/\\.\\w]+)?+(:[0-9]*+)?)";

	public HTMLBodyFormatter() {
	}

	@Override
	public void addAlternateFormat(MailBody mb, AttachmentManager am,
			Map<String, String> attachments) {
		String plain = mb.getValue("text/plain");
		String converted = convert(plain);
		mb.addConverted("text/html", converted);

	}

	private String convert(String plain) {
		StringBuilder sb = new StringBuilder();
		String escaped = StringEscapeUtils.escapeHtml(plain);
		escaped = escaped.replaceAll("\r\n", "<br>");
		escaped = escaped.replaceAll("\n", "<br>");
		sb.append(escaped);
		Pattern p = Pattern.compile(PATTERN_URL + "|" + PATTERN_EMAIL);
		Matcher m = p.matcher(sb);

		StringBuffer res = parseUrl(m, sb);
		if (res.toString().length() > 0) {
			return res.toString();
		} else {
			return sb.toString();
		}

	}

	@Override
	public boolean canConvert(String mime) {
		return "text/plain".equals(mime);
	}

	private StringBuffer parseUrl(Matcher m, StringBuilder input) {
		StringBuffer result = new StringBuffer();

		Pattern pEmail = Pattern.compile(PATTERN_EMAIL);
		Pattern pUrl = Pattern.compile(PATTERN_URL);

		while (m.find()) {
			String href = m.group();
			Matcher mEmail = pEmail.matcher(href);
			Matcher mUrl = pUrl.matcher(href);

			if (mUrl.find()) {
				if (href.startsWith("http")) {
					m.appendReplacement(result, "<a href=\"" + href
							+ "\" target=\"_blank\">" + href + "</a>");
				} else {
					m.appendReplacement(result, "<a href=\"http://" + href
							+ "\" target=\"_blank\">" + href + "</a>");
				}
			} else if (mEmail.find()) {
				m.appendReplacement(result, "<a href=mailto:" + href + ">"
						+ href + "</a>");
			}
		}
		m.appendTail(result);
		return result;
	}

}
