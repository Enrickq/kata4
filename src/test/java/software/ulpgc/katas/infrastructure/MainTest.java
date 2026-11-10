package software.ulpgc.katas.infrastructure;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    void invalidArgumentExitsWithFailure() throws Exception {
        var result = execute("abc");

        assertEquals(1, result.exitCode());
        assertTrue(result.output().contains("Argumentos incorrectos:"), result.output());
    }

    @Test
    void httpErrorExitsWithFailureAndReadableMessage() throws Exception {
        var result = executeAgainstServer(503, "Servicio no disponible");

        assertEquals(1, result.exitCode());
        assertTrue(result.output().contains("El servicio respondió con HTTP 503."), result.output());
    }

    @Test
    void invalidResponseExitsWithFailureWithoutBlamingArguments() throws Exception {
        var result = executeAgainstServer(200, "{\"values\":[1e10000]}");

        assertEquals(1, result.exitCode());
        assertTrue(result.output().contains("JSON no válido"), result.output());
        assertFalse(result.output().contains("Argumentos incorrectos:"), result.output());
    }

    @Test
    void successfulRequestExitsNormallyAndDisplaysSeries() throws Exception {
        var result = executeAgainstServer(200, "{\"values\":[0,1,1,2,3,5,8,13]}");

        assertEquals(0, result.exitCode());
        assertTrue(result.output().contains("Fibonacci (8 términos): 0, 1, 1, 2, 3, 5, 8, 13"),
                result.output());
    }

    private ExecutionResult executeAgainstServer(int status, String body) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/fibonacci", exchange -> {
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(status, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();
        try {
            return execute("8", "http://127.0.0.1:" + server.getAddress().getPort());
        } finally {
            server.stop(0);
        }
    }

    private ExecutionResult execute(String... args) throws Exception {
        String javaName = File.separatorChar == '\\' ? "java.exe" : "java";
        String java = Path.of(System.getProperty("java.home"), "bin", javaName).toString();
        String classPath = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(Gson.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        var command = new ArrayList<>(List.of(java,
                "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8",
                "-cp", classPath, Main.class.getName()));
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try {
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                throw new AssertionError("El cliente no terminó dentro de 10 segundos.");
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return new ExecutionResult(process.exitValue(), output);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
        }
    }

    private record ExecutionResult(int exitCode, String output) {}
}
