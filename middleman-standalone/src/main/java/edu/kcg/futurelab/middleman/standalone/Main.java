package edu.kcg.futurelab.middleman.standalone;

import javax.websocket.server.ServerContainer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import edu.kcg.futurelab.middleman.WebsocketServer;

public class Main {
	public static void main(String[] args) throws Exception {
		int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
		String contextPath = args.length > 1 ? args[1] : "/middleman1";
		String webbappDir = args.length > 2 ? args[2] : "./webapp";

		ServletHolder holderHome = new ServletHolder("webapp", DefaultServlet.class);
		holderHome.setInitParameter("resourceBase", webbappDir);
		holderHome.setInitParameter("dirAllowed", "true");
		holderHome.setInitParameter("pathInfoOnly", "true");

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath(contextPath);
		context.addServlet(holderHome, "/*");

		ServerContainer wscontainer =
				WebSocketServerContainerInitializer.configureContext(context);
		wscontainer.setDefaultMaxBinaryMessageBufferSize(8192*1024);
		wscontainer.setDefaultMaxTextMessageBufferSize(8192*1024);
		wscontainer.setDefaultMaxSessionIdleTimeout(1000 * 60 * 30);
		wscontainer.addEndpoint(WebsocketServer.class);

		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		server.addConnector(connector);
		server.setHandler(context);
		server.start();
		server.join();
	}
}
