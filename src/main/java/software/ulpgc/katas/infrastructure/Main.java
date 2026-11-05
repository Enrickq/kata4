package software.ulpgc.katas.infrastructure;

import software.ulpgc.katas.architecture.domain.FetchFibonacciSeriesUseCase;
import software.ulpgc.katas.architecture.ports.ServiceException;
import software.ulpgc.katas.infrastructure.adapters.HttpSeriesService;
import software.ulpgc.katas.infrastructure.adapters.JsonSeriesParser;
import software.ulpgc.katas.infrastructure.console.ConsolePresenter;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

/** Punto de composición: reúne puertos e implementaciones tecnológicas. */
public final class Main {
    private static final String DEFAULT_BASE_URL = "http://localhost:8081";

    public static void main(String[] args) {
        run(args);
    }

    static int run(String[] args) {
        ConsolePresenter presenter = new ConsolePresenter();
        try {
            if (args.length > 2) {
                throw new IllegalArgumentException("Uso: [términos] [URL base]");
            }
            int amount = args.length > 0 ? Integer.parseInt(args[0]) : 8;
            String baseUrl = args.length > 1 ? args[1] : DEFAULT_BASE_URL;

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .build();
            var service = new HttpSeriesService(client, URI.create(baseUrl), new JsonSeriesParser());
            var useCase = new FetchFibonacciSeriesUseCase(service);

            System.out.println(presenter.render(useCase.execute(amount)));
            return 0;
        } catch (ServiceException error) {
            System.err.println(presenter.renderError(error));
        } catch (IllegalArgumentException error) {
            System.err.println("Argumentos incorrectos: " + error.getMessage());
        }
        return 1;
    }
}
