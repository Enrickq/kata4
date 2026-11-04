package software.ulpgc.katas.infrastructure.console;

import org.junit.jupiter.api.Test;
import software.ulpgc.katas.architecture.model.Series;
import software.ulpgc.katas.architecture.ports.ServiceException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsolePresenterTest {
    private final ConsolePresenter presenter = new ConsolePresenter();

    @Test
    void displaysReadableSeries() {
        assertEquals("Fibonacci (4 términos): 0, 1, 1, 2",
                presenter.render(new Series(List.of(0, 1, 1, 2))));
    }

    @Test
    void translatesErrorsForUsers() {
        assertTrue(presenter.renderError(ServiceException.network(null)).contains("No se puede conectar"));
        assertTrue(presenter.renderError(ServiceException.httpStatus(404)).contains("404"));
        assertTrue(presenter.renderError(ServiceException.invalidResponse(null)).contains("JSON"));
    }
}
