package software.ulpgc.katas.infrastructure.adapters;

import software.ulpgc.katas.architecture.model.Series;
import software.ulpgc.katas.architecture.ports.SeriesParser;
import software.ulpgc.katas.architecture.ports.SeriesService;
import software.ulpgc.katas.architecture.ports.ServiceException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class HttpSeriesService implements SeriesService {

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(5);

    private final HttpClient client;
    private final URI baseUrl;
    private final SeriesParser parser;

    public HttpSeriesService(
            HttpClient client,
            URI baseUrl,
            SeriesParser parser
    ) {
        if (!"http".equalsIgnoreCase(baseUrl.getScheme())
                && !"https".equalsIgnoreCase(baseUrl.getScheme())) {
            throw new IllegalArgumentException(
                    "La URL base debe usar HTTP o HTTPS."
            );
        }

        if (baseUrl.getHost() == null
                || baseUrl.getQuery() != null
                || baseUrl.getFragment() != null) {
            throw new IllegalArgumentException(
                    "La URL base no es válida."
            );
        }

        this.client = client;
        this.baseUrl = baseUrl;
        this.parser = parser;
    }

    @Override
    public Series fetch(int amount) throws ServiceException {

        URI endpoint = URI.create(
                stripTrailingSlash(baseUrl.toString())
                        + "/fibonacci?n=" + amount
        );

        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw ServiceException.httpStatus(
                        response.statusCode()
                );
            }

            return parser.parse(response.body());

        } catch (IOException e) {
            throw ServiceException.network(e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ServiceException.network(e);
        }
    }

    private static String stripTrailingSlash(String url) {
        return url.replaceAll("/+$", "");
    }
}