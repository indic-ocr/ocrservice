package in.rkvsraman.indicocr.webservice;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.BradleyLocalThreshold;
import Catalano.Imaging.Filters.Grayscale;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class App extends AbstractVerticle {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		JsonObject object = new JsonObject();
		object.put("scribo_path", args[0]);
		object.put("http.port", 8081);

		Vertx.vertx().deployVerticle(App.class.getName(), new DeploymentOptions().setConfig(object));

	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		// TODO Auto-generated method stub

		System.out.println("ScriboPath:" + config().getString("scribo_path"));
		startWebApp(startFuture);
		System.out.println("Server started on :"+ config().getInteger("http.port"));
	}

	private void startWebApp(Future<Void> startFuture) {
		// Create a router object.
		Router router = Router.router(vertx);

		// Bind "/" to our hello message.
		router.route().handler(BodyHandler.create());
		router.get("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/html")
					.end("<html><body><h1>Indic OCR Service</h1><form action=\"/ocr\" ENCTYPE=\"multipart/form-data\" method=\"POST\" >"
							+ "choose a file to upload:<input type=\"file\" name=\"myfile\"/><br>"
							+ "<input type=\"text\" name=\"lang\" placeholder=\"eng\">"
							+ "<input type=\"text\" name=\"dpi\" placeholder=\"300\" >" 
							+ " <input type=\"submit\"/>"
							+ "</form> </body></html>");
		});

		router.post("/ocr").handler(this::getODT);

		router.post("/india").handler(this::getIndia);
		router.post("/indiastring").handler(this::getIndiaString);

		// Create the HTTP server and pass the "accept" method to the request
		// handler.
		HttpServerOptions serverOptions = new HttpServerOptions();
		serverOptions.setCompressionSupported(true);
		vertx.createHttpServer(serverOptions).requestHandler(router::accept).listen(

				config().getInteger("http.port", 8080), result -> {
					if (result.succeeded()) {
						startFuture.complete();
					} else {
						startFuture.fail(result.cause());
					}
				});
	}

	private void getODT(RoutingContext routingContext) {

		printRequestTime();
		if (routingContext.fileUploads().size() < 1) {
			routingContext.response().end("No file attached.\n");
			return;
		}
		String filePath = new String();
		for (FileUpload f : routingContext.fileUploads()) {

			filePath = f.uploadedFileName();
			System.out.println("Filepath:" + filePath);
			break;
		}
		// File f = new File(filePath);
		//
		// String mimetype = new MimetypesFileTypeMap().getContentType(f);
		// String type = mimetype.split("/")[0];
		// System.out.println("Mimetype: "+mimetype + " for " + filePath);
		// if (!type.equals("image")) {
		// routingContext.response().end("Uploaded file is not an image.\n");
		// return;
		// }

		String lang = routingContext.request().getFormAttribute("lang");
		String dpi = routingContext.request().getFormAttribute("dpi");
		System.out.println("DPI is:"+ dpi);
		if(dpi == null)
			dpi = "300";
		System.out.println("Lang:" + lang);
		if (lang == null || lang.length() == 0) {
			routingContext.response().end("No language specified.\n");
			return;
		}

		if (filePath.length() < 0) {
			routingContext.response().end("Could not retrieve uploaded file.\n");
			return;
		}

		convertToODTAndSend(routingContext, filePath, lang,dpi);

	}

	private void getIndia(RoutingContext routingContext) {

		printRequestTime();
		if (routingContext.fileUploads().size() < 1) {
			routingContext.response().end("No file attached.\n");
			return;
		}
		String filePath = new String();
		for (FileUpload f : routingContext.fileUploads()) {

			filePath = f.uploadedFileName();
			System.out.println("Filepath:" + filePath);
			break;
		}
		// File f = new File(filePath);
		//
		// String mimetype = new MimetypesFileTypeMap().getContentType(f);
		// String type = mimetype.split("/")[0];
		// System.out.println("Mimetype: "+mimetype + " for " + filePath);
		// if (!type.equals("image")) {
		// routingContext.response().end("Uploaded file is not an image.\n");
		// return;
		// }

		String sourcelang = routingContext.request().getFormAttribute("sourcelang");
		String tolang = routingContext.request().getFormAttribute("tolang");
		System.out.println("Lang:" + sourcelang + " " + tolang);
		if (sourcelang == null || sourcelang.length() == 0) {
			routingContext.response().end("No source language specified.\n");
			return;
		}
		if (tolang == null || tolang.length() == 0) {
			routingContext.response().end("No destination language specified.\n");
			return;
		}

		if (filePath.length() < 0) {
			routingContext.response().end("Could not retrieve uploaded file.\n");
			return;
		}

		binarizeAndRecognize(routingContext, filePath, sourcelang, tolang);

	}

	private void getIndiaString(RoutingContext routingContext) {

		printRequestTime();

		try {
			ExecutorService service = Executors.newSingleThreadExecutor();
			RequestSerializer serializer = new RequestSerializer(routingContext );
			AcrossIndiaStringTask ast = new AcrossIndiaStringTask(routingContext, serializer, config());

			service.execute(ast);
			System.out.println("Transliterating..");

		} catch (Exception e) {

			routingContext.response().end("Something broke!!");
			e.printStackTrace();
		}

	}

	private void binarizeAndRecognize(RoutingContext routingContext, String filePath, String sourcelang,
			String tolang) {

		try {
			

			CommandLine command = new CommandLine(config().getString("scribo_path"));

			File outputfile;
			try {
				outputfile = File.createTempFile("fromweb", ".xml");
			} catch (IOException e) {
				routingContext.response().end("Could not create intermediate files.\n");
				e.printStackTrace();
				return;
			}
			command.addArguments(filePath + " " + outputfile.getAbsolutePath() + " --ocr-lang " + sourcelang);
			System.out.println("Command is:" + command.toString());

			ExecuteWatchdog watchDog = new ExecuteWatchdog(60000); // Not more
																	// than
																	// 60
																	// seconds

			DefaultExecutor executor = new DefaultExecutor();
			executor.setWatchdog(watchDog);

			TessHandler handler = new TessHandler(routingContext, filePath, outputfile.getAbsolutePath(), watchDog,
					command, sourcelang, tolang, TessHandler.SCRIBO_COMMAND);

			try {
				executor.execute(command, handler);
			} catch (ExecuteException e) {
				routingContext.response().end("Some problem in intermediate process.\n");
				e.printStackTrace();
				return;
			} catch (IOException e) {
				routingContext.response().end("IO Exception in intermediate process.\n");
				e.printStackTrace();
				return;
			}
			System.out.println("Conversion to xml started...");

		}

		catch (Exception e) {
			routingContext.response().end("Could not complete the request\n");
			e.printStackTrace();
			return;
		}

	}

	private void convertToODTAndSend(RoutingContext routingContext, String filePath, String lang, String dpi) {

		CommandLine command = new CommandLine(config().getString("scribo_path"));

		File outputfile;
		try {
			outputfile = File.createTempFile("fromweb", ".xml");
		} catch (IOException e) {
			routingContext.response().end("Could not create intermediate files.\n");
			e.printStackTrace();
			return;
		}
		command.addArguments(filePath + " " + outputfile.getAbsolutePath() + " --ocr-lang " + lang);
		System.out.println("Command is:" + command.toString());

		ExecuteWatchdog watchDog = new ExecuteWatchdog(30000); // Not more than
																// 30 seconds

		DefaultExecutor executor = new DefaultExecutor();
		executor.setWatchdog(watchDog);

		ScriboHandler handler = new ScriboHandler(routingContext, filePath, outputfile, watchDog, command, lang,
				ScriboHandler.CONVERT_TO_ODT, dpi);

		try {
			executor.execute(command, handler);
		} catch (ExecuteException e) {
			routingContext.response().end("Some problem in intermediate process.\n");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			routingContext.response().end("IO Exception in intermediate process.\n");
			e.printStackTrace();
			return;
		}
		System.out.println("Conversion to xml started...");

	}

	private void printRequestTime() {

		SimpleDateFormat sd = new SimpleDateFormat("HH:mm:::dd:MM:yyyy");

		System.out.println("\n\n\nnew request arrives at :" + sd.format(new Date()));

	}

}
