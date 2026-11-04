package software.ulpgc.katas.infrastructure.adapters;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.katas.architecture.ports.ServiceException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpSeriesServiceTest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    void fetchesGetAndParsesJson() throws Exception {
        String[] requestedPath = new String[1];
        startServer(200, "{\"values\":[0,1,1,2,3]}", requestedPath);
        var series = adapter().fetch(5);
        assertEquals("/fibonacci?n=5", requestedPath[0]);
        assertEquals(5, series.values().size());
        assertEquals(3, series.values().getLast());
    }

    @Test
    void detectsHttpError() throws Exception {
        startServer(503, "Servicio caído", null);
        var error = assertThrows(ServiceException.class, () -> adapter().fetch(5));
        assertEquals(ServiceException.Kind.HTTP_STATUS, error.kind());
        assertEquals(503, error.statusCode());
    }

    @Test
    void detectsInvalidJsonReturnedByServer() throws Exception {
        startServer(200, "{\"values\":[1,]}", null);
        var error = assertThrows(ServiceException.class, () -> adapter().fetch(5));
        assertEquals(ServiceException.Kind.INVALID_RESPONSE, error.kind());
    }

    @Test
    void detectsEmptyResponse() throws Exception {
        startServer(200, "", null);
        var error = assertThrows(ServiceException.class, () -> adapter().fetch(5));
        assertEquals(ServiceException.Kind.INVALID_RESPONSE, error.kind());
    }

    @Test
    void detectsNetworkFailureWithoutCallingExternalServices() {
        // Puerto recién liberado; no presupone una API pública ni credenciales.
        try (var socket = new java.net.ServerSocket(0)) {
            int unusedPort = socket.getLocalPort();
            socket.close();
            var service = new HttpSeriesService(HttpClient.newHttpClient(),
                    URI.create("http://127.0.0.1:" + unusedPort), new JsonSeriesParser());
            var error = assertThrows(ServiceException.class, () -> service.fetch(5));
            assertEquals(ServiceException.Kind.NETWORK, error.kind());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void changingBaseUrlDoesNotChangeDomainCode() throws Exception {
        startServer(200, "{\"values\":[0]}", null);
        assertEquals(0, adapter().fetch(1).values().getFirst());
    }

    private HttpSeriesService adapter() {
        return new HttpSeriesService(HttpClient.newHttpClient(),
                URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
                new JsonSeriesParser());
    }

    private void startServer(int status, String body, String[] requestedPath) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/fibonacci", exchange -> {
            if (requestedPath != null) {
                requestedPath[0] = exchange.getRequestURI().toString();
            }
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            // -1 para cuerpo vacío evita un chunk HTTP sin datos.
            exchange.sendResponseHeaders(status, payload.length == 0 ? -1 : payload.length);
            if (payload.length > 0) exchange.getResponseBody().write(payload);
            exchange.close();
        });
        server.start();
    }
}
