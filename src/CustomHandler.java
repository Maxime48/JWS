import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;


public class CustomHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String query = exchange.getRequestURI().getQuery();

        URI ops = exchange.getRequestURI();

        String response = null;
        String filetype = "text";
        String extension = null;

        //handling extensions and filetype
        String uri = ops.getPath();
            //Test if this a directory and display index or not
            String file = Server.root + ops.getPath();
            if(
                    Files.isDirectory(Path.of(file))
            ){
                if(!Server.index){//is index set to false
                    //Display the index
                    String index = ( ops.getPath().endsWith("/") ) ?
                                    "index.html" : "/index.html";
                    file = file + index;
                    uri = ops.getPath()+index;
                }else{ //else display file/folder list
                    String[] list = new File(Server.root +  uri).list();
                    StringBuilder htmlDirectory = new StringBuilder();
                    htmlDirectory.append("<!DOCTYPE html>\n");
                    htmlDirectory.append("<html>\n");
                    htmlDirectory.append("  <head>\n");
                    htmlDirectory.append("    <meta charset=\"utf-8\">\n");
                    htmlDirectory.append("    <title>Directory listing - ").append(uri).append("</title>\n");
                    htmlDirectory.append("  </head>\n");
                    htmlDirectory.append("  <body>");
                    for(String element : list){
                            htmlDirectory.append("<a href=\"").append(
                                    (ops.getPath().endsWith("/")) ? element : ops.getPath() + "/" + element
                            ).append("\">");
                            htmlDirectory.append(element).append("\n");
                            htmlDirectory.append("</a><br>\n");
                    }
                    htmlDirectory.append("</body>");
                    htmlDirectory.append("</html>");
                    filetype = "text";
                    extension = "html";
                    response = htmlDirectory.toString();
                }
            }

            if(extension == null) {
                extension = (uri.contains(".")) ? uri.substring(uri.lastIndexOf(".") + 1) : "";
            }
        if (extension.equals("js")) {
            extension = "javascript";
        }
        if (extension.equals("png") || extension.equals("jpg")) {
            filetype = "image";
        }

        //reading requested file
        byte[] imageBytes = new byte[0];
        if(response == null) {
            if (filetype.equals("image")) {
                imageBytes = Files.readAllBytes(Path.of(file));
            } else {

                BufferedReader BufferedfileReader = new BufferedReader(
                        new FileReader(file)
                );
                response = BufferedfileReader.lines().collect(Collectors.joining());

            }
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
