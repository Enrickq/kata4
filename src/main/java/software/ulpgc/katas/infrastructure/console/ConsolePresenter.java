package software.ulpgc.katas.infrastructure.console;

import software.ulpgc.katas.architecture.model.Series;
import software.ulpgc.katas.architecture.ports.ServiceException;

import java.util.stream.Collectors;

public final class ConsolePresenter {

    public String render(Series series) {

        String numbers = series.values()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        return "Fibonacci ("
                + series.values().size()
                + " términos): "
                + numbers;
    }

    public String renderError(ServiceException error) {

        return switch (error.kind()) {

            case NETWORK ->
                    "No se puede conectar con el servicio. "
                            + "Revisa que esté iniciado y la URL base.";

            case HTTP_STATUS ->
                    "El servicio respondió con HTTP "
                            + error.statusCode() + ".";

            case INVALID_RESPONSE ->
                    "El servicio devolvió una respuesta vacía "
                            + "o con JSON no válido.";
        };
    }
}