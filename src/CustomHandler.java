import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.stream.Collectors;

public class CustomHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();

        String query = exchange.getRequestURI().getQuery();

        URI ops = exchange.getRequestURI();

        //reading requested file
        BufferedReader objReader = new BufferedReader(new FileReader(Server.root+ops.getPath()));
        String response = objReader.lines().collect(Collectors.joining());

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
