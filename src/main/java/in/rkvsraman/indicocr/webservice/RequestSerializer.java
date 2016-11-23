package in.rkvsraman.indicocr.webservice;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
		String tolang = obj.getString("tolang");
		String sourcelang = obj.getString("sourcelang");
		String operation = obj.getString("operation");
		String useEngine = obj.getString("engine");
		// System.out.println("Lang:" + img + " " + tolang);

		try {
			File filePath = new File(img);
			if (operation.equals("invert")) {
				BufferedImage bufferedImage = ImageIO.read(filePath);

				FastBitmap fbm = new FastBitmap(bufferedImage);

				Invert inv = new Invert();

				inv.applyInPlace(fbm);

				Grayscale grayscale = new Grayscale();
				grayscale.applyInPlace(fbm);

				BradleyLocalThreshold bld = new BradleyLocalThreshold();
				bld.applyInPlace(fbm);

				DocumentSkewChecker skewChecker = new DocumentSkewChecker();

				double angle = skewChecker.getSkewAngle(fbm);

				Rotate rotate = new Rotate(-angle, true);
				rotate.applyInPlace(fbm);

				filePath = File.createTempFile("beforerotate", ".png");
				ImageIO.write(fbm.toBufferedImage(), "png", filePath);

				Rectangle rect = RectangleUtils.getLargestRectangle(fbm.getWidth(), fbm.getHeight(), angle, 0);

				System.out.println(fbm.getWidth() + " " + fbm.getHeight() + " " + rect.getWidth() + " "
						+ rect.getHeight() + " " + angle + " " + rect.getX() + " " + rect.getY());
				Crop crop = new Crop((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(),
						(int) rect.getHeight());

				crop.ApplyInPlace(fbm);

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

				filePath = File.createTempFile("beforerotate", ".png");
				ImageIO.write(fbm.toBufferedImage(), "png", filePath);

				Rectangle rect = RectangleUtils.getLargestRectangle(fbm.getWidth(), fbm.getHeight(), angle, 0);

				System.out.println(fbm.getWidth() + " " + fbm.getHeight() + " " + rect.getWidth() + " "
						+ rect.getHeight() + " " + angle + " " + rect.getX() + " " + rect.getY());
				Crop crop = new Crop((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(),
						(int) rect.getHeight());

				crop.ApplyInPlace(fbm);

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
}
