import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Servicio auxiliar de prueba. No pertenece a la arquitectura del cliente. */
class FibonacciDemoServer {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8081;
        String mode = args.length > 1 ? args[1] : "ok";
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/fibonacci", exchange -> {
            String query = exchange.getRequestURI().getRawQuery();
            int status = "error".equals(mode) ? 503 : 200;
            String body;
            if ("malformed".equals(mode)) {
                body = "{\"values\":[0,1,]}";
            } else if ("empty".equals(mode)) {
                body = "";
            } else if ("error".equals(mode)) {
                body = "Servicio no disponible";
            } else {
                try {
                    if (query == null || !query.matches("n=\\d+")) {
                        throw new IllegalArgumentException();
                    }
                    int n = Integer.parseInt(query.substring(2));
                    if (n < 1 || n > 46) throw new IllegalArgumentException();
                    body = "{\"values\":" + fibonacci(n) + "}";
                } catch (IllegalArgumentException ex) {
                    status = 400;
                    body = "Parámetro n inválido";
                }
            }
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, response.length == 0 ? -1 : response.length);
            if (response.length > 0) exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        System.out.println("Servidor de demostración en http://localhost:" + port + "/fibonacci?n=8 [" + mode + "]");
        System.out.println("Para detenerlo, pulsa Ctrl+C.");
    }

    private static List<Integer> fibonacci(int amount) {
        List<Integer> values = new ArrayList<>();
        int previous = 0;
        int current = 1;
        for (int i = 0; i < amount; i++) {
            values.add(previous);
            int next = previous + current;
            previous = current;
            current = next;
        }
        return values;
    }
}
