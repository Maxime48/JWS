import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Server{

    private Integer port;
    private String root;

    public static Boolean index;
    public static String accept;
    public static String reject;


    public Server() {
        try {
            //Set params there
        } catch (Exception e) {
            System.out.println("Default parameters have been set due to the following error: ");
            e.printStackTrace();
        }
        this.port = (this.port == null) ? 80 : this.port;
        this.root = (this.root == null) ? "/" : this.root;
        Server.index = Server.index != null && Server.index;
        Server.accept = (Server.accept == null) ? "" : Server.accept;
        Server.reject = (Server.reject == null) ? "" : Server.reject;
    }

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(8500), 0);
        HttpContext context = server.createContext("/");
        context.setHandler(new CustomHandler());
        server.start();

    }

}