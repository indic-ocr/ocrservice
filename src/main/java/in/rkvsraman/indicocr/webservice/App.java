package in.rkvsraman.indicocr.webservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class App extends AbstractVerticle {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		

		Vertx.vertx().deployVerticle(App.class.getName());

	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		// TODO Auto-generated method stub
		
		
		startWebApp( startFuture); 
	}

	

	private void startWebApp(Future<Void> startFuture) {
		// Create a router object.
		Router router = Router.router(vertx);

		// Bind "/" to our hello message.
		router.get("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/html").end("<h1>Hello from my first Vert.x 3 application</h1>");
		});

		// router.route("/assets/*").handler(StaticHandler.create("assets"));
		//
		 router.get("/ocr").handler(this::getAll);
		// router.route("/api/whiskies*").handler(BodyHandler.create());
		// router.post("/api/whiskies").handler(this::addOne);
		// router.get("/api/whiskies/:id").handler(this::getOne);
		// router.put("/api/whiskies/:id").handler(this::updateOne);
		// router.delete("/api/whiskies/:id").handler(this::deleteOne);

		// Create the HTTP server and pass the "accept" method to the request
		// handler.
		vertx
	     .createHttpServer()
	     .requestHandler(router::accept)
	     .listen(
	         
	         config().getInteger("http.port", 8080),
	         result -> {
	           if (result.succeeded()) {
	             startFuture.complete();
	           } else {
	             startFuture.fail(result.cause());
	           }
	         }
	     );
	}
	
	private void getAll(RoutingContext routingContext) { 
		System.out.println( System.getProperty("user.dir"));
		routingContext.request().response().sendFile("/home/raman/ocr/olena/olena/scribo/src/content_in_doc");
	    
	  } 

}
