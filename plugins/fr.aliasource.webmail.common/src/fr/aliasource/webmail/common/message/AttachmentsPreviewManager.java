package fr.aliasource.webmail.common.message;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.utils.RunnableExtensionLoader;
import fr.aliasource.webmail.common.Activator;
import fr.aliasource.webmail.common.IAccount;

public class AttachmentsPreviewManager {

	// private IAccount ac;
	private AttachmentManager atmgr;

	private Map<String, IPreviewGenerator> generators;

	private Log logger = LogFactory.getLog(getClass());

	public AttachmentsPreviewManager(IAccount ac, AttachmentManager atmgr) {
		// this.ac = ac;
		this.atmgr = atmgr;
		this.generators = new HashMap<String, IPreviewGenerator>();
		RunnableExtensionLoader<IPreviewGenerator> rel = new RunnableExtensionLoader<IPreviewGenerator>();
		List<IPreviewGenerator> exts = rel.loadExtensions(Activator.PLUGIN_ID,
				"previewer", "previewer", "implementation");
		for (IPreviewGenerator pg : exts) {
			registerGenerator(pg);
		}
		logger.info(exts.size() + " attachments previewer registered");
	}

	public void registerGenerator(IPreviewGenerator pg) {
		Set<String> mimes = pg.getSupportedTypes();
		for (String m : mimes) {
			generators.put(m, pg);
		}
	}

	public boolean isPreviewable(String mimeType) {
		if (mimeType == null) {
			return false;
		}
		
		boolean ret = generators.containsKey(mimeType);

		return ret;
	}

	public boolean hasPreview(String attachId) {
		return new File(atmgr.getAttachDir() + File.separator + attachId
				+ ".preview.bin").exists();
	}

	public void computePreview(String atId, String mime, File af) {
		IPreviewGenerator pg = generators.get(mime);
		pg.computePreview(atId, mime, af);
	}

	public String getPreviewMimeType(String mime) {
		IPreviewGenerator pg = generators.get(mime);
		return pg.getPreviewMimeType(mime);
	}

}
