package software.ulpgc.katas.architecture.domain;

import software.ulpgc.katas.architecture.model.Series;
import software.ulpgc.katas.architecture.ports.SeriesService;
import software.ulpgc.katas.architecture.ports.ServiceException;

/** Orquesta el caso de uso. No sabe cómo se obtiene la serie. */
public final class FetchFibonacciSeriesUseCase {
    private final SeriesService service;

    public FetchFibonacciSeriesUseCase(SeriesService service) {
        this.service = service;
    }

    public Series execute(int amount) throws ServiceException {
        // El servidor del profesor usa int para Fibonacci (46 términos sin desbordamiento).
        if (amount < 1 || amount > 46) {
            throw new IllegalArgumentException("El número de términos debe estar entre 1 y 46.");
        }
        return service.fetch(amount);
    }
}
