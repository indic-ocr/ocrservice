package in.rkvsraman.indicocr.utils;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class RectangleUtils {

	/**
	 * Return a largest Rectangle that will fit in a rotated image
	 * 
	 * @param imgWidth
	 *            Width of image
	 * @param imgHeight
	 *            Height of Image
	 * @param rotAngDeg
	 *            Rotation angle in degrees
	 * @param type
	 *            0 = Largest Area, 1 = Smallest Area, 2 = Widest, 3 = Tallest
	 * @return
	 */
	public static Rectangle getLargestRectangle(double imageWidth, double imageHeight, double rotAngDeg, int type) {
		Rectangle rect = null;

		double ang = Math.toRadians(rotAngDeg);

		int quadrant = (new Double(Math.floor(ang / (Math.PI / 2)))).intValue() & 3;
		double sign_alpha = (quadrant & 1) == 0 ? ang : Math.PI - ang;
		double alpha = (sign_alpha % Math.PI + Math.PI) % Math.PI;

		double newWidth = imageWidth * Math.cos(alpha) + imageHeight * Math.sin(alpha);
		double newHeight = imageWidth * Math.sin(alpha) + imageHeight * Math.cos(alpha);

		double gamma = imageWidth < imageHeight ? Math.atan2(newWidth, newHeight) : Math.atan2(newHeight, newWidth);

		double delta = Math.PI - alpha - gamma;

		double length = imageWidth < imageHeight ? imageHeight : imageWidth;
		double d = length * Math.cos(alpha);
		double a = d * Math.sin(alpha) / Math.sin(delta);

		double y = a * Math.cos(gamma);
		double x = y * Math.tan(gamma);

		rect = new Rectangle();

		System.out.println(x + " " + y + " " + (newWidth - 2 * x) + " " + (newHeight - 2 * y));
		rect.setRect(x, y, newWidth - 2 * x, newHeight - 2 * y);

		return rect;

	}

	public static BufferedImage getCroppedImage(BufferedImage source) {

		boolean flag = false;
		int upperBorder = -1;
		do {
			upperBorder++;
			for (int c1 = 0; c1 < source.getWidth(); c1++) {
				if (source.getRGB(c1, upperBorder) != Color.white.getRGB()) {
					flag = true;
					break;
				}
			}

			if (upperBorder >= source.getHeight())
				flag = true;
		} while (!flag);

		BufferedImage destination = new BufferedImage(source.getWidth(), source.getHeight() - upperBorder,
				BufferedImage.TYPE_INT_ARGB);

		return destination;
	}

}
