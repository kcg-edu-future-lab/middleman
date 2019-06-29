package edu.kcg.futurelab.middleman.standalone;

import javax.websocket.server.ServerContainer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import edu.kcg.futurelab.middleman.DefaultService;
import edu.kcg.futurelab.middleman.sample.SimplePaintService;

public class Main {
	public static void main(String[] args) throws Exception {
		ServletHolder holderHome = new ServletHolder("webapp", DefaultServlet.class);
		holderHome.setInitParameter("resourceBase", "./webapp");
		holderHome.setInitParameter("dirAllowed", "true");
		holderHome.setInitParameter("pathInfoOnly", "true");

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/middleman");
		context.addServlet(holderHome, "/*");

		ServerContainer wscontainer =
				WebSocketServerContainerInitializer.configureContext(context);
		wscontainer.addEndpoint(DefaultService.class);
		wscontainer.addEndpoint(SimplePaintService.class);

		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(8080);
		server.addConnector(connector);
		server.setHandler(context);
		server.start();
		server.join();
	}
}
