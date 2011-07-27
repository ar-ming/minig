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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.mime.MimePart;
import org.minig.imap.mime.MimeTree;

public class BodyStructureParser {

	private static final Log logger = LogFactory
			.getLog(BodyStructureParser.class);

	private PartDescriptionParser partParser;
	
	public BodyStructureParser() {
		this.partParser = new PartDescriptionParser(this);
	}
	
	public MimeTree parse(byte[] bs) {
	  MimeTree ret = new MimeTree();
		parse(ret, bs);
		if (logger.isDebugEnabled()) {
			logger.debug("mime tree:\n" + ret);
		}
		return ret;
	}

	private char charAt(byte[] bytes, int i) {
		return (char) bytes[i];
	}
	
	private byte[] substring(byte[] bytes, int start, int end) {
		byte[] ret = new byte[end - start];
		System.arraycopy(bytes, start, ret, 0, ret.length);
		return ret;
	}

	private byte[] substring(byte[] bytes, int start) {
		return substring(bytes, start, bytes.length);
	}
	
	public void parse(MimePart parent, byte[] bs) {
		if (bs == null || bs.length == 0 || charAt(bs, 0) != '(') {
			return;
		}
		//System.err.println("parse parent:\n"+parent+"\n"+bs+"\n---");

		if (charAt(bs, 1) != '(') {
			int endIdx = ParenMatcher.closingParenIndex(bs, 0) + 1;
			byte[] single = substring(bs, 0, endIdx);
			MimePart part = parseSinglePart(single);
			parent.addPart(part);
			if (endIdx < bs.length) {
			  byte[] next = substring(bs, endIdx);
			  // logger.info("next: " + next);
			  parse(parent, next);
			}
		} else {
			int endIdx = ParenMatcher.closingParenIndex(bs, 1) + 1;
			byte[] sub = substring(bs, 1, endIdx);
			MimePart mp = new MimePart();
			parent.addPart(mp);
			parse(mp, sub);
			if (endIdx < bs.length) {
			  byte[] next = substring(bs, endIdx);
			  // logger.info("next: " + next);
			  parse(mp, next);
			}
			int nextBlock = ParenMatcher.closingParenIndex(bs, 0) + 1;
			byte[] next = substring(bs, nextBlock);
			parse(parent, next);
		}
	}

	private MimePart parseSinglePart(byte[] substring) {
		MimePart singlePart = new MimePart();
		try {
			partParser.parse(singlePart, substring);
		} catch (RuntimeException t) {
			logger.error("Error parsing part: " + substring);
			throw t;
		}

		return singlePart;
	}

}
