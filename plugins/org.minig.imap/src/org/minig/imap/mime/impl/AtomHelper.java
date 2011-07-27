package org.minig.imap.mime.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.utils.FileUtils;

public final class AtomHelper {

	private static final Log logger = LogFactory.getLog(AtomHelper.class);

	private static final byte[] closingBraquet = "}".getBytes();
	
	public static final byte[] getFullResponse(String resp, InputStream followUp) {
		String orig = resp;
		byte[] envelData = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		try {
			out.write(orig.getBytes());
			if (followUp != null) {
				out.write(closingBraquet);
				FileUtils.transfer(followUp, out, true);
			}
		} catch (IOException e) {
			logger.error("error loading stream part of answer", e);
		}
		envelData = out.toByteArray();
		return envelData;
	}

}
