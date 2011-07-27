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

package fr.aliasource.webmail.common.message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.mime.BodyParam;
import org.minig.imap.mime.MimePart;
import org.w3c.dom.Document;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.utils.FileUtils;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.ICloseListener;

/**
 * Stores attachments in a temporary place before mail sending. Metadata about
 * attachments are store in a separate XML file.
 * 
 * @author tom
 * 
 */
public class AttachmentManager {

	private IAccount account;
	private AtomicInteger attachIdAllocator;
	private AttachmentsPreviewManager previewer;

	public static final String META_FILENAME = "filename";
	public static final String META_SIZE = "size";
	public static final String META_MIME = "mime";
	public static final String META_CONTENT_ID = "content-id";
	public static final String META_PREVIEW = "preview";
	public static final String META_PREVIEW_MIME = "preview-mime";

	private static final Log logger = LogFactory
			.getLog(AttachmentManager.class);

	public AttachmentManager(IAccount account) {
		this.account = account;
		attachIdAllocator = new AtomicInteger();
		new File(getAttachDir()).mkdirs();
		this.previewer = new AttachmentsPreviewManager(account, this);
	}

	public String getAttachDir() {
		return account.getCache().getCachePath() + File.separator
				+ "attachements";
	}

	/**
	 * Allocates a unique identifier for handling a new attachment.
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public String allocateAttachementId() throws InterruptedException {
		int newAttach = attachIdAllocator.incrementAndGet();
		return "at_" + System.currentTimeMillis() + "-" + newAttach;
	}

	/**
	 * Stores metadata & content of an attachment
	 * 
	 * @param atid
	 * @param metadata
	 * @param attach
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 * @throws TransformerException
	 */
	public String store(String atid, Map<String, String> metadata,
			InputStream attach) throws InterruptedException, IOException,
			ParserConfigurationException, FactoryConfigurationError,
			TransformerException {

		String attachFile = getAttachDir() + File.separator + atid + ".bin";
		File af = new File(attachFile);
		FileUtils.transfer(attach, new FileOutputStream(af), true);
		metadata.put(META_SIZE, "" + af.length());
		metadata.put(META_PREVIEW, "" + isPreviewable(metadata));
		if (isPreviewable(metadata)) {
			metadata.put(META_PREVIEW_MIME, previewer.getPreviewMimeType(metadata
					.get(META_MIME)));
		}
		storeMetadata(atid, metadata);
		return atid;
	}

	private void computePreview(Map<String, String> metadata, File af,
			String atId) {
		previewer.computePreview(atId, metadata.get(META_MIME), af);
	}

	private boolean isPreviewable(Map<String, String> meta) {
		String m = meta.get(META_MIME);
		return previewer.isPreviewable(m);
	}

	/**
	 * Verifies if a given attachment id exists and has data associated.
	 * 
	 * @param attachId
	 * @return
	 */
	public boolean isValidId(String attachId) {
		return open(attachId).exists();
	}

	public File open(String attachId) {
		final File data = new File(getAttachDir() + File.separator + attachId
				+ ".bin");
		final File meta = new File(getAttachDir() + File.separator + attachId
				+ ".xml");

		account.addCloseListener(new ICloseListener() {
			@Override
			public void accountClosed(IAccount ac) {
				if (logger.isDebugEnabled()) {
					logger.debug("deleting " + meta.getAbsolutePath());
					logger.debug("deleting " + data.getAbsolutePath());
				}
				meta.delete();
				data.delete();
			}
		});

		return data;
	}

	/**
	 * Loads metadata from disk for the given attachment id
	 * 
	 * @param attachId
	 * @return
	 */
	public Map<String, String> getMetadata(String attachId) {
		Map<String, String> ret = new HashMap<String, String>();

		try {
			Document doc = DOMUtils.parse(new FileInputStream(getAttachDir()
					+ File.separator + attachId + ".xml"));
			ret.put(META_FILENAME, DOMUtils.getElementText(doc
					.getDocumentElement(), "filename"));
			ret.put(META_SIZE, DOMUtils.getElementText(
					doc.getDocumentElement(), "size"));
			ret.put(META_MIME, DOMUtils.getElementText(
					doc.getDocumentElement(), "mime"));
			ret.put(META_CONTENT_ID, DOMUtils.getElementText(doc
					.getDocumentElement(), META_CONTENT_ID));
			String prev = DOMUtils.getElementText(doc.getDocumentElement(),
					META_PREVIEW);
			ret.put(META_PREVIEW, prev);
			if ("true".equals(prev)) {
				ret.put(META_PREVIEW_MIME, DOMUtils.getElementText(doc
						.getDocumentElement(), META_PREVIEW_MIME));
			}

		} catch (Exception e) {
			logger.error("Error loading metadata for " + attachId);
		}

		return ret;
	}

	public void storeMetadata(String id, MimePart part, long size)
			throws FileNotFoundException, ParserConfigurationException,
			FactoryConfigurationError, TransformerException {
		Map<String, String> meta = new HashMap<String, String>();
		meta.put(META_FILENAME, getFilenameFromMimePart(part));
		meta.put(META_SIZE, size + "");
		meta.put(META_MIME, part.getFullMimeType());
		meta.put(META_CONTENT_ID, part.getContentId());
		meta.put(META_PREVIEW, "" + isPreviewable(meta));
		if (isPreviewable(meta)) {
			meta.put(META_PREVIEW_MIME, previewer.getPreviewMimeType(meta
					.get(META_MIME)));
		}
		storeMetadata(id, meta);
	}

	private String getFilenameFromMimePart(MimePart part) {
		BodyParam bodyParam = part.getBodyParam("name");
		if (bodyParam != null) {
			return bodyParam.getValue();
		}
		return null;
	}
	
	private void storeMetadata(String id, Map<String, String> meta)
			throws ParserConfigurationException, FactoryConfigurationError,
			FileNotFoundException, TransformerException {
		String metafile = getAttachDir() + File.separator + id + ".xml";
		String datafile = getAttachDir() + File.separator + id + ".bin";
		Document doc = newMetadataDocument();

		fixMeta(meta);

		// Attachment may have no name !
		// e.g. image as attachment
		String filename = meta.get(META_FILENAME);
		if (filename == null || filename.isEmpty()) {
			if (meta.containsKey(META_CONTENT_ID)
					&& meta.get(META_CONTENT_ID) != null) {
				filename = "cid:"
						+ meta.get(META_CONTENT_ID).substring(1,
								meta.get(META_CONTENT_ID).length() - 1);
			} else {
				filename = "noname";
			}
		}

		DOMUtils.createElementAndText(doc.getDocumentElement(), "filename",
				filename);
		DOMUtils.createElementAndText(doc.getDocumentElement(), "size", meta
				.get(META_SIZE));
		DOMUtils.createElementAndText(doc.getDocumentElement(), "mime", meta
				.get(META_MIME));

		if (meta.containsKey(META_CONTENT_ID)) {
			DOMUtils.createElementAndText(doc.getDocumentElement(),
					"content-id", meta.get(META_CONTENT_ID));
		}

		boolean preview = "true".equals(meta.get(META_PREVIEW));
		DOMUtils.createElementAndText(doc.getDocumentElement(), META_PREVIEW, ""+preview);
		if (preview) {
			DOMUtils.createElementAndText(doc.getDocumentElement(),
					META_PREVIEW_MIME, meta.get(META_PREVIEW_MIME));
		}

		DOMUtils.serialise(doc, new FileOutputStream(metafile));

		if (isPreviewable(meta)) {
			computePreview(meta, new File(datafile), id);
		}

	}

	private void fixMeta(Map<String, String> meta) {
		String f = meta.get(META_FILENAME);
		if (f != null && f.length() > 0) {
			if (f.endsWith(".pdf")) {
				meta.put(META_MIME, "application/pdf");
			}
		}
	}

	private Document newMetadataDocument() throws ParserConfigurationException,
			FactoryConfigurationError {
		Document doc = DOMUtils.createDoc(
				"http://obm.aliasource.fr/xsd/attach_metadata",
				"attachementMetadata");
		return doc;
	}

	public boolean hasPreview(String id) {
		return previewer.hasPreview(id);
	}

}
