import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class CustomHandler implements HttpHandler {

    public static ArrayList<String> addressTobinary(String Adress){
        String[] octetArray = Adress.split("\\.");
        ArrayList<String> binaryAdress = new ArrayList<>();
        for (String string : octetArray){
            int octet = Integer.parseInt(string);
            StringBuilder binaryOctet = new StringBuilder(Integer.toBinaryString(octet));
            if(binaryOctet.length()!=8){
                int missingBinary = 8 - binaryOctet.length();
                while(missingBinary>0){
                    binaryOctet.insert(0, "0");
                    missingBinary--;
                }
            }
            binaryAdress.add(binaryOctet.toString());
        }
        return binaryAdress;
    }

    public static ArrayList<String> specialSplit(String Adress){
        return new ArrayList<>(Arrays.asList(Adress.split("/")));
    }

    public static ArrayList<String> extractNetwork(ArrayList<String> BinaryIp, String mask){
        int imask = Integer.parseInt(mask);
        for(int i = 0; i<BinaryIp.size();i++){
            String part = BinaryIp.get(i);
            StringBuilder newPart = new StringBuilder();
            for(int j=0; j<part.length();j++){
                if(imask>0){
                    newPart.append(part.charAt(j));
                }else{
                    newPart.append(0);
                }
                imask--;
            }
            BinaryIp.set(i, newPart.toString());
        }
        return BinaryIp;
    }

    public void handle(HttpExchange exchange) throws IOException {
        URI ops = exchange.getRequestURI();

        String response = null;
        String filetype = "text";
        String extension = null;
        int responseCode = 200;

        String ClientAdress = exchange.getRemoteAddress().getAddress().getHostAddress(); //extracting remote adress
        if(!Objects.equals(ClientAdress, "0:0:0:0:0:0:0:1")) {
            ArrayList<String> BinaryClientAdress = CustomHandler.addressTobinary(ClientAdress);

            if (!Objects.equals(Server.accept, "")) {
                ArrayList<String> acceptSplited = CustomHandler.specialSplit(Server.accept);
                ArrayList<String> acceptNetworIPBinary = CustomHandler.addressTobinary(acceptSplited.get(0));
                System.out.println(acceptNetworIPBinary);

                ArrayList<String> clientNetwork = CustomHandler.extractNetwork(BinaryClientAdress, acceptSplited.get(1));

                System.out.println(clientNetwork);

                if (!clientNetwork.equals(acceptNetworIPBinary)) {
                    responseCode = 403;
                }

            }
            if (!Objects.equals(Server.reject, "")) {
                ArrayList<String> rejectSplited = CustomHandler.specialSplit(Server.reject);
                ArrayList<String> rejectNetworIPBinary = CustomHandler.addressTobinary(rejectSplited.get(0));

                ArrayList<String> clientNetwork = CustomHandler.extractNetwork(BinaryClientAdress, rejectSplited.get(1));

                if (clientNetwork.equals(rejectNetworIPBinary)) {
                    responseCode = 403;
                }

            }
        }
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
                try {
                    BufferedReader BufferedfileReader = new BufferedReader(
                            new FileReader(file)
                    );
                    response = BufferedfileReader.lines().collect(Collectors.joining());
                }catch(IOException ignored){}
            }
        }

        if(responseCode == 403){
            filetype = "text";
            extension = "html";
            response = "403 - Unauthorized access";
        }

        if(response == null && imageBytes.length == 0){
            responseCode = 404;
            filetype = "text";
            extension = "html";
            response = "404 - Page not found";
        }

        //Headers
        exchange.getResponseHeaders().set(
                "Content-Type", filetype + "/" + extension + ";" + " charset=UTF-8"
        );

        exchange.sendResponseHeaders(responseCode,
            (filetype.equals("image")) ? imageBytes.length : response.getBytes().length
        );
        OutputStream os = exchange.getResponseBody();
        os.write(
                (filetype.equals("image")) ? imageBytes : response.getBytes()
        );
        os.close();
    }

}
