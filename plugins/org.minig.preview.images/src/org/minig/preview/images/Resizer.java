package org.minig.preview.images;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Resizer {

	private Log logger = LogFactory.getLog(getClass());

	public InputStream scaleImage(InputStream sourceImage, int wantedWidth,
			int wantedHeight) throws Exception {

		if (sourceImage == null) {
			logger.warn("received null source image. nothing to resize");
			return null;
		}

		InputStream imageStream = new BufferedInputStream(sourceImage);
		Image image = (Image) ImageIO.read(imageStream);

		if (image == null) {
			logger.warn("un-readable image. nothing to resize");
			return null;
		}
		
		int thumbWidth = wantedWidth;
		int thumbHeight = wantedHeight;

		// Make sure the aspect ratio is maintained, so the image is not skewed
		double thumbRatio = (double) thumbWidth / (double) thumbHeight;
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		double imageRatio = (double) imageWidth / (double) imageHeight;
		if (thumbRatio < imageRatio) {
			thumbHeight = (int) (thumbWidth / imageRatio);
		} else {
			thumbWidth = (int) (thumbHeight * imageRatio);
		}

		// Draw the scaled image
		BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = thumbImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

		return writeImage(thumbImage);
	}

	private InputStream writeImage(BufferedImage thumbImage) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(thumbImage, "png", out);
		return new ByteArrayInputStream(out.toByteArray());
	}

}
