import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CustomHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String query = exchange.getRequestURI().getQuery();

        URI ops = exchange.getRequestURI();

        //handling extensions and filetype
        String uri = ops.getPath();
        String extension = (uri.contains(".")) ? uri.substring(uri.lastIndexOf(".") + 1) : "";
        if (extension.equals("js")) {
            extension = "javascript";
        }
        String filetype = "text";
        if (extension.equals("png") || extension.equals("jpg")) {
            filetype = "image";
        }

        //reading requested file
        String response;
        byte[] imageBytes = new byte[0];
        if (filetype.equals("image")) {
            imageBytes = Files.readAllBytes(Path.of(Server.root + ops.getPath()));
            response = new String(imageBytes);
        } else {
            BufferedReader BufferedfileReader = new BufferedReader(
                    new FileReader(Server.root + ops.getPath())
            );
            response = BufferedfileReader.lines().collect(Collectors.joining());
        }

        //Headers
        exchange.getResponseHeaders().set(
                "Content-Type", filetype + "/" + extension + ";" + " charset=UTF-8"
        );

        exchange.sendResponseHeaders(200,
            (filetype.equals("image")) ? imageBytes.length : response.getBytes().length
        );
        OutputStream os = exchange.getResponseBody();
        os.write(
                (filetype.equals("image")) ? imageBytes : response.getBytes()
        );
        os.close();
    }

}
