package org.minig.preview.images.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.minig.preview.images.Resizer;

import fr.aliasource.utils.FileUtils;

import junit.framework.TestCase;

public class ResizerTest extends TestCase {

	public void testCtor() {
		Resizer r = new Resizer();
		assertNotNull(r);
	}

	public void testResize() throws Exception {
		Resizer r = new Resizer();
		assertNotNull(r);
		InputStream resized = r.scaleImage(loadSource(), 50, 50);
		assertNotNull(resized);
		File tmp = File.createTempFile("resized", ".png");
		FileUtils.transfer(resized, new FileOutputStream(tmp), true);
		System.err.println("resized file stored into " + tmp.getAbsolutePath());
	}

	private InputStream loadSource() {
		return getClass().getClassLoader().getResourceAsStream("data/bigg.png");
	}

}
