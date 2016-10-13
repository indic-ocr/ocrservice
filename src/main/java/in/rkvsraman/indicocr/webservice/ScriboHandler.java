package in.rkvsraman.indicocr.webservice;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;

import io.vertx.ext.web.RoutingContext;

public class ScriboHandler implements ExecuteResultHandler {

	
	
	private RoutingContext context;
	private String filePath;
	private File outputFile;
	private ExecuteWatchdog watchDog;
	private CommandLine command;
	private String language;

	public  ScriboHandler(RoutingContext context) {
		this.context = context;
	}
	public ScriboHandler(RoutingContext routingContext, String filePath, File outputfile, ExecuteWatchdog watchDog,
			CommandLine command, String language) {
		
		this.context = routingContext;
		this.filePath = filePath;
		this.outputFile = outputfile;
		this.watchDog = watchDog;
		this.command = command;
		this.language = language;
		
	}
	@Override
	public void onProcessComplete(int exitValue) {
		
		convertToODT();

	}

	private void convertToODT() {
		ExecutorService service = Executors.newSingleThreadExecutor();
		
		ODTConverter converter = new ODTConverter(context,filePath,outputFile,command , language);
		ODTTask task = new ODTTask(context,filePath,outputFile,command , language,converter);
		
		service.execute(task);
		System.out.println("Converting to ODT..");
		
	}
	@Override
	public void onProcessFailed(ExecuteException exception) {
		if(watchDog.killedProcess()){
			context.response().end("Conversion took more than  30 seconds. Aborting...\n");
		}
		else
		{
			context.response().end("Some problem with intermediate process. Aborting...\n");
			exception.printStackTrace();
			
		}
		
		return;

	}

}
