package in.rkvsraman.indicocr.webservice;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;

import io.vertx.ext.web.RoutingContext;

public class AcrossIndiaTask extends FutureTask<String> {

	private RoutingContext routingContext;
	private String sourcelang;
	private String filePath;
	private String tolang;


	public AcrossIndiaTask(Callable<String> callable) {
		super(callable);
		// TODO Auto-generated constructor stub
	}
	
	
	public AcrossIndiaTask(RoutingContext routingContext, String filePath, String sourcelang, String tolang, BinaryImageConverter converter){
		super(converter);
		this.routingContext=routingContext;
		this.filePath=filePath;
		this.sourcelang= sourcelang;
		this.tolang= tolang;
	}
	
	@Override
	protected void done() {
		if(isCancelled()){
			routingContext.response().end("Could not convert to ODT file. Aborting...\n");
			return;
		}
		String imagePath = new String();
		try {
			imagePath = get();
		} catch (InterruptedException e) {
			routingContext.response().end("Could not convert to ODT file. Aborting...\n");
			e.printStackTrace();
			return;
		} catch (ExecutionException e) {
			routingContext.response().end("Could not convert to ODT file. Aborting...\n");
			e.printStackTrace();
			return;
		}
		System.out.println("Imagepath is:" + imagePath);
		if(imagePath.length() > 0){
			String recognizedtext = "recoed" + System.currentTimeMillis();

			CommandLine tessCommand = new CommandLine("tesseract");
			tessCommand.addArguments(imagePath + " " + recognizedtext + " -l " + sourcelang);

			System.out.println("Command is:" + tessCommand.toString());

			ExecuteWatchdog watchDog = new ExecuteWatchdog(30000); // Not more
																	// than
																	// 30
																	// seconds

			DefaultExecutor executor = new DefaultExecutor();
			executor.setWatchdog(watchDog);

			TessHandler handler = new TessHandler(routingContext,imagePath, recognizedtext,
					watchDog, tessCommand, sourcelang, tolang, TessHandler.TESSERACT_COMMAND);

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
		
		
	}

}
