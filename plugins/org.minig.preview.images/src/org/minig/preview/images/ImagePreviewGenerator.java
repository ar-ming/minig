package org.minig.preview.images;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.utils.FileUtils;
import fr.aliasource.webmail.common.message.IPreviewGenerator;

public class ImagePreviewGenerator implements IPreviewGenerator {

	private HashSet<String> mimes;
	private Log logger = LogFactory.getLog(getClass());

	public ImagePreviewGenerator() {
		mimes = new HashSet<String>();
		mimes.add("image/png");
		mimes.add("image/jpg");
		mimes.add("image/jpeg");
	}

	@Override
	public void computePreview(String attachId, String mime, File toPreview) {
		String origPath = toPreview.getAbsolutePath();
		Resizer r = new Resizer();
		String targetPath = origPath.replace(attachId, attachId + ".preview");
		
		try {
			FileInputStream in = new FileInputStream(origPath);
			InputStream resized = r.scaleImage(in, 75, 75);
			FileUtils.transfer(resized, new FileOutputStream(targetPath), true);
		} catch (Exception e) {
			logger.error("error computing preview", e);
		}
	}

	@Override
	public String getPreviewMimeType(String mime) {
		return "image/png";
	}

	@Override
	public Set<String> getSupportedTypes() {
		return mimes;
	}

}
