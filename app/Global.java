import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.plannifico.server.PlannificoRESTListener;

import play.*;

public class Global extends GlobalSettings {

	@Override
    public void onStart(Application app) {
		/*
		URI baseUri = UriBuilder.fromUri ("http://localhost/").port(9998).build();
		
		Logger.debug ("baseUri: " + baseUri);
		
		ResourceConfig config = 
				new ResourceConfig (PlannificoRESTListener.class)
				.packages("org.glassfish.jersey.examples.jackson")
		        .register(JacksonFeature.class);		
		
		Logger.debug ("config: " + config.toString());
		
		JdkHttpServerFactory.createHttpServer (baseUri, config);  
		
		Logger.debug ("HttpServer ok");
		*/
    }
}
