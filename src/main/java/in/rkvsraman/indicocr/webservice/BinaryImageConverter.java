package in.rkvsraman.indicocr.webservice;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BradleyLocalThreshold;
import Catalano.Imaging.Filters.Grayscale;
import io.vertx.ext.web.RoutingContext;

public class BinaryImageConverter implements Callable<String> {

	
	private RoutingContext context;
	private String filePath;
	private String sourcelang;
	private String tolang;
	public  BinaryImageConverter(RoutingContext routinContext, String filePath, String sourcelang, String tolang) {
		this.context = routinContext;
		this.filePath = filePath;
		this.sourcelang= sourcelang;
		this.tolang=tolang;
	}
	@Override
	public String call() throws Exception {
		BufferedImage image = ImageIO.read(new File(filePath));

		FastBitmap fbm = new FastBitmap(image);

		Grayscale gray = new Grayscale();

		gray.applyInPlace(fbm);

		BradleyLocalThreshold bt = new BradleyLocalThreshold();
		bt.applyInPlace(fbm);

		File tempImageFile = File.createTempFile("indiafile", ".png");

		ImageIO.write(fbm.toBufferedImage(), "png", tempImageFile);
		return tempImageFile.getAbsolutePath();
		
	}

}
