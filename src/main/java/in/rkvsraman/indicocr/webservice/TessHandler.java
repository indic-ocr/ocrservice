package in.rkvsraman.indicocr.webservice;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.odftoolkit.simple.draw.FrameRectangle;
import org.odftoolkit.simple.draw.Textbox;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions.HorizontalRelative;
import org.odftoolkit.simple.style.StyleTypeDefinitions.VerticalRelative;
import org.odftoolkit.simple.text.Paragraph;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BernsenThreshold;
import Catalano.Imaging.Filters.BradleyLocalThreshold;
import Catalano.Imaging.Filters.CannyEdgeDetector;
import Catalano.Imaging.Filters.Grayscale;
import Catalano.Imaging.Tools.Blob;
import Catalano.Imaging.Tools.BlobDetection;
import Catalano.Imaging.Tools.BlobExtractor;
import in.rkvsraman.ocr.olena.scribocli.ObjectFactory;
import in.rkvsraman.ocr.olena.scribocli.PcGts;
import in.rkvsraman.ocr.olena.scribocli.PcGts.Page;
import in.rkvsraman.ocr.olena.scribocli.PcGts.Page.TextRegion;
import in.rkvsraman.ocr.olena.scribocli.PcGts.Page.TextRegion.Line;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class TessHandler implements ExecuteResultHandler {

	private RoutingContext context;
	private String filePath;
	private File outputFile;
	private ExecuteWatchdog watchDog;
	private CommandLine command;
	private String language;
	private String recognizedtext;
	private String sourcelang;
	private String tolang;
	private int commandType;
	public static final int TESSERACT_COMMAND = 1;
	public static final int SCRIBO_COMMAND = 2;

	public TessHandler(RoutingContext context) {
		this.context = context;
	}

	public TessHandler(RoutingContext routingContext, String filePath, File outputfile, ExecuteWatchdog watchDog,
			CommandLine command, String language) {

		this.context = routingContext;
		this.filePath = filePath;
		this.outputFile = outputfile;
		this.watchDog = watchDog;
		this.command = command;
		this.language = language;

	}

	public TessHandler(RoutingContext routingContext, String absolutePath, String recognizedtext,
			ExecuteWatchdog watchDog, CommandLine command, String sourcelang, String tolang, int commandType) {
		// TODO Auto-generated constructor stub
		this.context = routingContext;
		this.filePath = absolutePath;
		this.recognizedtext = recognizedtext;
		this.watchDog = watchDog;
		this.command = command;
		this.sourcelang = sourcelang;
		this.tolang = tolang;
		this.commandType = commandType;
	}

	@Override
	public void onProcessComplete(int exitValue) {
		if (commandType == TESSERACT_COMMAND)
			doTesseract();
		else
			doScribo();

	}

	private void doScribo() {
		try {
			JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
			File xml = new File(recognizedtext);

			PcGts pcgts = (PcGts) jc.createUnmarshaller().unmarshal(xml);
			Page page = pcgts.getPage();
			StringBuffer sb = new StringBuffer();
			List<TextRegion> textRegions = page.getTextRegion();
			for (TextRegion textRegion : textRegions) {
				List<Line> textLines = textRegion.getLines();
				for (Line textLine : textLines) {

					if (textLine.getText() != null && textLine.getText().trim().length() > 0)
						sb.append(textLine.getText().trim() + " ");
				}

			}
			sendResponseText(sb.toString());

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	private void sendResponseText(String source) {
		String sourceString = "";
		String toEnglish = "";
		String transliteratedString = "";
		if (sourcelang.equals("eng")) {
			sourceString = source;
		} else {
			sourceString = OCRPostProcessor.getProcessedString(sourcelang, source);
		}

		if (sourcelang.equals("eng")) {

			toEnglish = sourceString;
		} else
			toEnglish = VarnamUtils.getEnglishString(sourceString, sourcelang);
		if (toEnglish != null && !tolang.equals("eng")) {
			
			 transliteratedString = VarnamUtils.transliterate(toEnglish, tolang);
			JsonObject returnObject = new JsonObject();
			returnObject.put("recognizedText", sourceString);
			returnObject.put("englishTransliteration", toEnglish);
			returnObject.put("tranliteratedTo", transliteratedString);
			returnObject.put("filePath", this.filePath);
			System.out.println(returnObject.toString());
			context.response().end(returnObject.toString());

		} else {
			JsonObject returnObject = new JsonObject();
			returnObject.put("recognizedText", sourceString);
			returnObject.put("englishTransliteration", toEnglish);
			returnObject.put("tranliteratedTo", toEnglish);
			returnObject.put("filePath", this.filePath);

			System.out.println(returnObject.toString());

			context.response().end(returnObject.toString());

		}
		// checkForegroundAndBackground();

	}

	private void checkForegroundAndBackground() {
		// TODO Auto-generated method stub
		try {

			FastBitmap fbm = new FastBitmap(ImageIO.read(new File(filePath)));

			Grayscale gray = new Grayscale();
			gray.applyInPlace(fbm);

			ImageIO.write(fbm.toBufferedImage(), "png", File.createTempFile("gray", ".png"));

			FastBitmap newFbm = new FastBitmap(fbm);

			BradleyLocalThreshold blt = new BradleyLocalThreshold();
			blt.applyInPlace(newFbm);

			ImageIO.write(newFbm.toBufferedImage(), "png", File.createTempFile("brad", ".png"));

			newFbm = new FastBitmap(fbm);

			BernsenThreshold bt = new BernsenThreshold();

			bt.applyInPlace(newFbm);

			ImageIO.write(newFbm.toBufferedImage(), "png", File.createTempFile("bern", ".png"));

			CannyEdgeDetector cn = new CannyEdgeDetector();

			cn.applyInPlace(fbm);

			ImageIO.write(fbm.toBufferedImage(), "png", File.createTempFile("canny", ".png"));
			BlobDetection bd = new BlobDetection();

			List<Blob> blobs = bd.ProcessImage(fbm);
			System.out.println("Blobs size:" + blobs.size());

			BlobExtractor be = new BlobExtractor();

			for (Blob b : blobs) {
				FastBitmap temp = be.ExtractBox(fbm, b);
				ImageIO.write(temp.toBufferedImage(), "png", File.createTempFile("blob", ".png"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void doTesseract() {
		try {
			String currDir = Paths.get(".").toAbsolutePath().normalize().toString();
			File output = new File(currDir + File.separator + recognizedtext + ".txt");
			System.out.println("Output file:" + output.getAbsolutePath());
			BufferedReader br;

			br = new BufferedReader(new FileReader(output));

			StringBuffer sb = new StringBuffer();
			String s = new String();

			while ((s = br.readLine()) != null) {
				s = s.trim();
				if (s.length() > 0)
					sb.append(s + " ");
			}
			br.close();
			sendResponseText(sb.toString());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // TODO Auto-generated method stub

	}

	@Override
	public void onProcessFailed(ExecuteException exception) {
		if (watchDog.killedProcess()) {
			System.out.println("It is more than 30 seconds now");
			context.response().end("Conversion took more than  30 seconds. Aborting...\n");
			exception.printStackTrace();
		} else {
			context.response().end("Some problem with intermediate process. Aborting...\n");
			exception.printStackTrace();

		}

		return;

	}

}
