package software.ulpgc.katas.architecture.domain;

import org.junit.jupiter.api.Test;
import software.ulpgc.katas.architecture.model.Series;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FetchFibonacciSeriesUseCaseTest {
    @Test
    void dependsOnlyOnItsServicePort() throws Exception {
        var useCase = new FetchFibonacciSeriesUseCase(amount -> new Series(List.of(0, 1, 1)));
        assertEquals(List.of(0, 1, 1), useCase.execute(3).values());
    }

    @Test
    void rejectsOutOfRangeInputBeforeCallingService() {
        var useCase = new FetchFibonacciSeriesUseCase(amount -> { throw new AssertionError("No debe llamar al servicio"); });
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(0));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(47));
    }
}
