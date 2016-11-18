package in.rkvsraman.indicocr.webservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;

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
			ExecuteWatchdog watchDog, CommandLine command, String sourcelang, String tolang) {
		// TODO Auto-generated constructor stub
		this.context = routingContext;
		this.filePath = absolutePath;
		this.recognizedtext = recognizedtext;
		this.watchDog = watchDog;
		this.command = command;
		this.sourcelang = sourcelang;
		this.tolang = tolang;
	}

	@Override
	public void onProcessComplete(int exitValue) {
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
			String sourceString = "";
			if (sourcelang.equals("eng"))
				sourceString = sb.toString();
			else
				sourceString = OCRPostProcessor.getProcessedString(sourcelang, sb.toString());

			String toEnglish = "";
			if (sourcelang.equals("eng") )
				toEnglish = sourceString;
			else
				toEnglish = VarnamUtils.getEnglishString(sourceString, sourcelang);
			if (toEnglish != null && !tolang.equals("eng")) {
				String transliteratedString = VarnamUtils.transliterate(toEnglish, tolang);
				JsonObject returnObject = new JsonObject();
				returnObject.put("recognizedText", sourceString);
				returnObject.put("englishTransliteration", toEnglish);
				returnObject.put("tranliteratedTo", transliteratedString);
				System.out.println(returnObject.toString());
				context.response().end(returnObject.toString());

			} else {
				JsonObject returnObject = new JsonObject();
				returnObject.put("recognizedText", sourceString);
				returnObject.put("englishTransliteration", toEnglish);

				System.out.println(returnObject.toString());

				context.response().end(returnObject.toString());

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
