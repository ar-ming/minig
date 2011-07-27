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

package org.minig.imap.mime.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.mime.BodyParam;
import org.minig.imap.mime.MimePart;

public class PartDescriptionParser extends ParenListParser {

	private BodyStructureParser bsp;

	private static final Log logger = LogFactory
			.getLog(PartDescriptionParser.class);

	public PartDescriptionParser(BodyStructureParser bsp) {
		this.bsp = bsp;
	}

	public void parse(MimePart part, byte[] s) {
		int parsePosition = 1;
		parsePosition = consumeToken(parsePosition, s);
		part.setMimeType(new String(lastReadToken));
		parsePosition = consumeToken(parsePosition, s);
		part.setMimeSubtype(new String(lastReadToken));

		// body parameter paren list
		parsePosition = consumeToken(parsePosition, s);
		Set<BodyParam> bodyParams = parseBodyParams();
		part.setBodyParams(bodyParams);

		// content id
		parsePosition = consumeToken(parsePosition, s);
		part.setContentId(new String(lastReadToken));

		// content description
		parsePosition = consumeToken(parsePosition, s);

		// content transfer encoding
		parsePosition = consumeToken(parsePosition, s);
		part.setContentTransfertEncoding(new String(lastReadToken));

		// size
		parsePosition = consumeToken(parsePosition, s);

		if ("message/rfc822".equals(part.getFullMimeType())) {
			parseNested(part, s);
		} else {
			// line count or attachment infos (rfc2060 p61)
			parsePosition = consumeToken(parsePosition, s);

			if (parsePosition < s.length) {
				parsePosition = consumeToken(parsePosition, s);
				byte[] remaining = substring(s, parsePosition, s.length);
				if (startsWith(remaining, " (\"ATTACHMENT")) {
					parsePosition = consumeToken(parsePosition, s);
				}

				if (startsWith(lastReadToken, "\"ATTACHMENT\" (")) {
					byte[] list = substring(lastReadToken, "\"ATTACHMENT\" ("
							.length(), lastReadToken.length - 1);
					lastTokenType = TokenType.LIST;
					lastReadToken = list;
					Set<BodyParam> bp = parseBodyParams();
					bodyParams.addAll(bp);
				}
			}
		}
	}

	private Set<BodyParam> parseBodyParams() {
		Set<BodyParam> params = new HashSet<BodyParam>();
		if (lastTokenType == TokenType.LIST) {
			int idx = 0;
			byte[] list = lastReadToken;
			while (idx < list.length) {
				idx = consumeToken(idx, list);
				String key = new String(lastReadToken);
				idx = consumeToken(idx, list);
				String value = new String(lastReadToken);
				BodyParam param = constructBodyParam(key, value);
				params.add(param);
			}
		}
		return params;
	}

	private BodyParam constructBodyParam(String key, String value) {
		BodyParamParser parser = new BodyParamParser(key, value);
		return parser.parse();
	}
	
	private void parseNested(MimePart part, byte[] s) {
		if (logger.isDebugEnabled()) {
			logger.debug("parse nested:\n" + new String(s));
		}

		int position = 1;

		// skip most tokens as they were parsed by the caller

		// Mime Type (MESSAGE)
		position = consumeToken(position, s);
		// Sub Mime Type (RFC822)
		position = consumeToken(position, s);
		// Body parameters
		position = consumeToken(position, s);
		// NIL
		position = consumeToken(position, s);
		// NIL
		position = consumeToken(position, s);
		// content transfer encoding
		position = consumeToken(position, s);
		// size
		position = consumeToken(position, s);
		// headers
		position = consumeToken(position, s);

		// embedded part of the rfc822 section
		consumeToken(position, s);
		bsp.parse(part, lastReadToken);
	}

}
