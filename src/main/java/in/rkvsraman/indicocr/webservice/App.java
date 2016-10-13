package in.rkvsraman.indicocr.webservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class App extends AbstractVerticle {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Vertx.vertx().deployVerticle(App.class.getName());

	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		// TODO Auto-generated method stub

		startWebApp(startFuture);
	}

	private void startWebApp(Future<Void> startFuture) {
		// Create a router object.
		Router router = Router.router(vertx);

		// Bind "/" to our hello message.
		router.route().handler(BodyHandler.create());
		router.get("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/html").end("<h1>Indic - OCR Service</h1> <h2> By RKVS Raman</h2> ");
		});

		
		router.post("/ocr").handler(this::getAll);
		
		// Create the HTTP server and pass the "accept" method to the request
		// handler.
		vertx.createHttpServer().requestHandler(router::accept).listen(

				config().getInteger("http.port", 8080), result -> {
					if (result.succeeded()) {
						startFuture.complete();
					} else {
						startFuture.fail(result.cause());
					}
				});
	}

	private void getAll(RoutingContext routingContext) {
		
		routingContext.response().setChunked(true);
		for (FileUpload f : routingContext.fileUploads()) {

			System.out.println(f.name() + " " + f.fileName() + " " + f.uploadedFileName());
		}

		System.out.println(routingContext.request().getFormAttribute("name"));

		routingContext.response().end("Got it!!!\n");

	}

}
