package in.rkvsraman.indicocr.webservice;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class AcrossIndiaStringTask extends FutureTask<JsonObject> {

	private RoutingContext routingContext;
	private JsonObject config;

	public AcrossIndiaStringTask(Callable<JsonObject> callable) {
		super(callable);
		// TODO Auto-generated constructor stub
	}

	public AcrossIndiaStringTask(RoutingContext routingContext, RequestSerializer converter, JsonObject config) {
		super(converter);
		this.routingContext = routingContext;
		this.config = config;

	}

	@Override
	protected void done() {
		if (isCancelled()) {
			routingContext.response().end("Could not convert to ODT file. Aborting...\n");
			return;
		}

		try {
			JsonObject parsedObject = get();
			if (parsedObject == null) {
				if (!routingContext.response().ended())
					routingContext.response().end("Could not complete OCR process ... aborting");
				return;
			}

			ExecuteWatchdog watchDog = new ExecuteWatchdog(60000); // Not more
																	// than
																	// 30
																	// seconds

			DefaultExecutor executor = new DefaultExecutor();
			executor.setWatchdog(watchDog);

			if (parsedObject.getString("engine").equals("scribo")) {
				CommandLine command = new CommandLine(config.getString("scribo_path"));

				File outputfile;
				try {
					outputfile = File.createTempFile("fromweb", ".xml");
				} catch (IOException e) {
					routingContext.response().end("Could not create intermediate files.\n");
					e.printStackTrace();
					return;
				}
				command.addArguments(parsedObject.getString("filePath") + " " + outputfile.getAbsolutePath()
						+ " --ocr-lang " + parsedObject.getString("sourcelang"));
				System.out.println("Command is:" + command.toString());

				TessHandler handler = new TessHandler(routingContext, parsedObject.getString("filePath"),
						outputfile.getAbsolutePath(), watchDog, command, parsedObject.getString("sourcelang"),
						parsedObject.getString("tolang"), TessHandler.SCRIBO_COMMAND);

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
			}
			if (parsedObject.getString("engine").equals("tesseract")) {
				String recognizedtext = "recoed" + System.currentTimeMillis();

				CommandLine tessCommand = new CommandLine("tesseract");
				tessCommand.addArguments(parsedObject.getString("filePath") + " " + recognizedtext + " -l "
						+ parsedObject.getString("sourcelang"));

				System.out.println("Command is:" + tessCommand.toString());

				TessHandler handler = new TessHandler(routingContext, parsedObject.getString("filePath"),
						recognizedtext, watchDog, tessCommand, parsedObject.getString("sourcelang"),
						parsedObject.getString("tolang"), TessHandler.TESSERACT_COMMAND);

				try {
					executor.execute(tessCommand, handler);
				} catch (ExecuteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (InterruptedException e) {
			routingContext.response().end("Could not convert to ODT file. Aborting...\n");
			e.printStackTrace();
			return;
		} catch (ExecutionException e) {
			routingContext.response().end("Could not convert to ODT file. Aborting...\n");
			e.printStackTrace();
			return;
		}

	}

}
