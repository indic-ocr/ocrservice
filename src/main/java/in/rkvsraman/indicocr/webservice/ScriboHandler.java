package in.rkvsraman.indicocr.webservice;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;

import io.vertx.ext.web.RoutingContext;

public class ScriboHandler implements ExecuteResultHandler {

	
	
	private RoutingContext context;

	public  ScriboHandler(RoutingContext context) {
		this.context = context;
	}
	@Override
	public void onProcessComplete(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProcessFailed(ExecuteException arg0) {
		// TODO Auto-generated method stub

	}

}
