package in.rkvsraman.indicocr.webservice;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BradleyLocalThreshold;
import Catalano.Imaging.Filters.Crop;
import Catalano.Imaging.Filters.Grayscale;
import Catalano.Imaging.Filters.Invert;
import Catalano.Imaging.Filters.Rotate;
import Catalano.Imaging.Tools.DocumentSkewChecker;
import in.rkvsraman.indicocr.utils.RectangleUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class RequestSerializer implements Callable<JsonObject> {
	private RoutingContext context;

	public RequestSerializer(RoutingContext routinContext) {
		this.context = routinContext;

	}

	@Override
	public JsonObject call() throws Exception {
		JsonObject obj = context.getBodyAsJson();

		String img = obj.getString("filePath");
		if (img.startsWith("https://")) { // Hack for fb messenger
			img = getFilePathFromURL(img);
		}
		if (img.startsWith("data:")) { // Hack for chrome data url

			img = getFilePathFromDataURL(img);
			if(img == null){
				context.response().end("Could not read image!!.\n");
				return null;
			}
		}
		String tolang = obj.getString("tolang");
		if (!App.langProps.stringPropertyNames().contains(tolang)) {
			context.response().end("Target language not supported.\n");
			return null;
		}
		String sourcelang = obj.getString("sourcelang");
		if (!App.langProps.stringPropertyNames().contains(sourcelang)) {
			context.response().end("Source language not supported.\n");
			return null;
		}
		String operation = obj.getString("operation");
		String useEngine = obj.getString("engine");
		// System.out.println("Lang:" + img + " " + tolang);

		try {
			File filePath = new File(img);
			if (operation.equals("invert")) {
				BufferedImage bufferedImage = ImageIO.read(filePath);

				FastBitmap fbm = new FastBitmap(bufferedImage);

				Grayscale grayscale = new Grayscale();
				grayscale.applyInPlace(fbm);
				Invert inv = new Invert();

				inv.applyInPlace(fbm);

				ImageIO.write(fbm.toBufferedImage(), "png", File.createTempFile("invbefore", ".png"));

				BradleyLocalThreshold bld = new BradleyLocalThreshold();
				bld.applyInPlace(fbm);

				DocumentSkewChecker skewChecker = new DocumentSkewChecker();

				double angle = skewChecker.getSkewAngle(fbm);

				Rotate rotate = new Rotate(-angle, true);
				rotate.applyInPlace(fbm);

				/*
				 * filePath = File.createTempFile("beforerotate", ".png");
				 * ImageIO.write(fbm.toBufferedImage(), "png", filePath);
				 * 
				 * Rectangle rect =
				 * RectangleUtils.getLargestRectangle(fbm.getWidth(),
				 * fbm.getHeight(), angle, 0);
				 * 
				 * System.out.println(fbm.getWidth() + " " + fbm.getHeight() +
				 * " " + rect.getWidth() + " " + rect.getHeight() + " " + angle
				 * + " " + rect.getX() + " " + rect.getY()); if (rect.getWidth()
				 * > 0.0 && rect.getHeight() > 0.0 && rect.getX() > 0.0 &&
				 * rect.getY() > 0.0) { Crop crop = new Crop((int) rect.getX(),
				 * (int) rect.getY(), (int) rect.getWidth(), (int)
				 * rect.getHeight());
				 * 
				 * crop.ApplyInPlace(fbm); }
				 */

				filePath = File.createTempFile("inverted", ".png");

				ImageIO.write(fbm.toBufferedImage(), "png", filePath);
			}
			if (operation.equals("binarize")) {
				BufferedImage bufferedImage = ImageIO.read(filePath);

				FastBitmap fbm = new FastBitmap(bufferedImage);

				Grayscale grayscale = new Grayscale();
				grayscale.applyInPlace(fbm);

				BradleyLocalThreshold bld = new BradleyLocalThreshold();
				bld.applyInPlace(fbm);
				DocumentSkewChecker skewChecker = new DocumentSkewChecker();

				double angle = skewChecker.getSkewAngle(fbm);

				Rotate rotate = new Rotate(-angle, true);
				rotate.applyInPlace(fbm);

				/*
				 * filePath = File.createTempFile("beforerotate", ".png");
				 * ImageIO.write(fbm.toBufferedImage(), "png", filePath);
				 * 
				 * Rectangle rect =
				 * RectangleUtils.getLargestRectangle(fbm.getWidth(),
				 * fbm.getHeight(), angle, 0);
				 * 
				 * System.out.println(fbm.getWidth() + " " + fbm.getHeight() +
				 * " " + rect.getWidth() + " " + rect.getHeight() + " " + angle
				 * + " " + rect.getX() + " " + rect.getY()); if (rect.getWidth()
				 * > 0.0 && rect.getHeight() > 0.0 && rect.getX() > 0.0 &&
				 * rect.getY() > 0.0) { Crop crop = new Crop((int) rect.getX(),
				 * (int) rect.getY(), (int) rect.getWidth(), (int)
				 * rect.getHeight());
				 * 
				 * crop.ApplyInPlace(fbm); }
				 */
				filePath = File.createTempFile("binary", ".png");

				ImageIO.write(fbm.toBufferedImage(), "png", filePath);
			}
			if (sourcelang == null || sourcelang.length() == 0) {
				context.response().end("No source language specified.\n");
				return null;
			}
			if (tolang == null || tolang.length() == 0) {
				context.response().end("No destination language specified.\n");
				return null;
			}

			if (filePath.getAbsolutePath().length() < 0) {
				context.response().end("Could not retrieve uploaded file.\n");
				return null;
			}

			JsonObject returnObject = new JsonObject();
			returnObject.put("filePath", filePath.getAbsolutePath());
			returnObject.put("sourcelang", sourcelang);
			returnObject.put("tolang", tolang);
			returnObject.put("engine", useEngine);

			return returnObject;

		} catch (IOException e) {

			context.response().end("Something broke!!");
			e.printStackTrace();
		}

		return null;

	}

	private String getFilePathFromDataURL(String img) {
		// TODO Auto-generated method stub

		System.out.println(img);
		if(!img.startsWith("data:image"))
			return null;
		String base64Image = img.split(",")[1];
		if (base64Image != null && base64Image.length() > 0) {
			byte[] imageBytes = DatatypeConverter.parseBase64Binary(base64Image);

			try {
				File f = File.createTempFile("dimage", ".png");
				BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(f));
				bout.write(imageBytes);
				bout.flush();
				bout.close();
				return f.getAbsolutePath();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	private String getFilePathFromURL(String img) {
		String returnURL = img;
		try {
			System.out.println("Reading url:" + img);
			URL url = new URL(img);
			BufferedImage bif = ImageIO.read(url);
			File f = File.createTempFile("fbimage", ".png");

			ImageIO.write(bif, "png", f);
			return f.getAbsolutePath();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return returnURL;

	}
}
