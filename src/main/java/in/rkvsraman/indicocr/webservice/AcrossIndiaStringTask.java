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

	public AcrossIndiaStringTask(Callable<JsonObject> callable) {
		super(callable);
		// TODO Auto-generated constructor stub
	}

	public AcrossIndiaStringTask(RoutingContext routingContext, RequestSerializer converter) {
		super(converter);
		this.routingContext = routingContext;

	}

	@Override
	protected void done() {
		if (isCancelled()) {
			routingContext.response().end("Could not convert to ODT file. Aborting...\n");
			return;
		}

		try {
			JsonObject parsedObject = get();

			CommandLine command = new CommandLine("/home/raman/ocr/olena/olena/scribo/src/content_in_doc");

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

			ExecuteWatchdog watchDog = new ExecuteWatchdog(60000); // Not more
																	// than
																	// 30
																	// seconds

			DefaultExecutor executor = new DefaultExecutor();
			executor.setWatchdog(watchDog);

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
