import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;

public class Main {
  private static final String LOCATION_FILE = "locations.json";

  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(4567), 0);
    server.createContext("/", new HtmlHandler());
    server.createContext("/updateLocation", new LocationHandler());
    server.createContext("/tailwind.css", new CssHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
    System.out.println("Server started at http://localhost:4567/");
  }

  static class HtmlHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
      if ("GET".equals(exchange.getRequestMethod())) {
        InputStream is = new FileInputStream("index.html");
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, is.available());
        OutputStream os = exchange.getResponseBody();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
          os.write(buffer, 0, bytesRead);
        }
        os.close();
        is.close();
      } else {
        exchange.sendResponseHeaders(405, -1); // Method Not Allowed
      }
    }
  }

  static class LocationHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
      if ("POST".equals(exchange.getRequestMethod())) {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
          sb.append(line);
        }
        saveLocation(sb.toString());
        exchange.sendResponseHeaders(200, "Location updated".length());
        OutputStream os = exchange.getResponseBody();
        os.write("Location updated".getBytes());
        os.close();
      } else {
        exchange.sendResponseHeaders(405, -1);
      }
    }

    private void saveLocation(String locationJson) {
      try (FileWriter fileWriter = new FileWriter(LOCATION_FILE)) {
        fileWriter.write(locationJson);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  static class CssHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
      if ("GET".equals(exchange.getRequestMethod())) {
        InputStream is = new FileInputStream("tailwind.css");
        exchange.getResponseHeaders().set("Content-Type", "text/css");
        exchange.sendResponseHeaders(200, is.available());
        OutputStream os = exchange.getResponseBody();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
          os.write(buffer, 0, bytesRead);
        }
        os.close();
        is.close();
      } else {
        exchange.sendResponseHeaders(405, -1); // Method Not Allowed
      }
    }
  }

}

