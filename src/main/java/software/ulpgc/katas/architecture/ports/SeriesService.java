package software.ulpgc.katas.architecture.ports;

import software.ulpgc.katas.architecture.model.Series;

/** Lo que necesita la aplicación; no revela HTTP ni Javalin. */
public interface SeriesService {
    Series fetch(int amount) throws ServiceException;
}
