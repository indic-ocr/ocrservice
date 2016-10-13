package in.rkvsraman.indicocr.webservice;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.commons.exec.CommandLine;

import io.vertx.ext.web.RoutingContext;

public class ODTTask extends FutureTask<String> {

	private RoutingContext context;
	private String filePath;
	private File outputFile;
	private CommandLine command;
	private String language;

	public ODTTask(Callable<String> callable) {
		super(callable);
		// TODO Auto-generated constructor stub
	}
	
	public ODTTask(RoutingContext context, String filePath, File outputFile, CommandLine command, String language,
			ODTConverter converter) {
		super(converter);
		this.context = context;
		this.filePath = filePath;
		this.outputFile = outputFile;
		
		this.command = command;
		this.language = language;
	}

	@Override
	protected void done() {
		if(isCancelled()){
			context.response().end("Could not convert to ODT file. Aborting...\n");
			return;
		}
		String odtFilePath = new String();
		try {
			odtFilePath = get();
		} catch (InterruptedException e) {
			context.response().end("Could not convert to ODT file. Aborting...\n");
			e.printStackTrace();
			return;
		} catch (ExecutionException e) {
			context.response().end("Could not convert to ODT file. Aborting...\n");
			e.printStackTrace();
			return;
		}
		
		if(odtFilePath.length() > 0){
			context.response().sendFile(odtFilePath);
		}
		
		
	}

}
