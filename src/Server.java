import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Server{

    private Integer port;

    public static String root;
    public static Boolean index;
    public static String accept;
    public static String reject;


    public Server() {
        try {
            //creating a constructor of file class and parsing an XML file
            File file = new File("config.xml");
            //an instance of factory that gives a document builder
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //an instance of builder to parse the specified xml file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            NodeList elm = doc.getElementsByTagName("webconf");
                Node node = elm.item(0);
                for(int i = 1; i<node.getChildNodes().getLength(); i++){
                    String parameterName = node.getChildNodes().item(i).getNodeName()
                            .replaceAll("[\\n\\r\\s]+", "");
                    if(!parameterName.equals("#text")){
                        String content = node.getChildNodes().item(i).getTextContent()
                                .replaceAll("[\\n\\r\\s]+", "");

                        System.out.println(parameterName + ": " + content);

                        if(parameterName.equals("port")){
                            this.port = Integer.parseInt(content);
                        }
                        if(parameterName.equals("root")){
                            Server.root = content;
                        }
                        if(parameterName.equals("index")){
                            Server.index = Boolean.parseBoolean(content);
                        }
                        if(parameterName.equals("accept")){
                            Server.accept = content;
                        }
                        if(parameterName.equals("reject")){
                            Server.reject = content;
                        }
                    }
                }

        } catch (Exception e) {
            System.out.println("Default parameters have been set due to the following error: ");
            e.printStackTrace();
        }
        this.port = (this.port == null) ? 80 : this.port;
        Server.root = (Server.root == null) ? "/" : Server.root;
        Server.index = Server.index != null && Server.index;
        Server.accept = (Server.accept == null) ? "" : Server.accept;
        Server.reject = (Server.reject == null) ? "" : Server.reject;
    }

    public static void main(String[] args) throws IOException {
        Server conf = new Server();
        HttpServer server = HttpServer.create(new InetSocketAddress(conf.port), 0);
        HttpContext context = server.createContext("/");
        context.setHandler(new CustomHandler());
        server.start();

    }

}