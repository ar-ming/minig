package fr.aliasource.webmail.common.message;

import java.io.File;
import java.util.Set;

public interface IPreviewGenerator {

	public Set<String> getSupportedTypes();
	
	public void computePreview(String attachId, String mime, File toPreview);
	
	public String getPreviewMimeType(String mime);
	
}
