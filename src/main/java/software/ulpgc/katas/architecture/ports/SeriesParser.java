package software.ulpgc.katas.architecture.ports;

import software.ulpgc.katas.architecture.model.Series;

/** Contrato para traducir el cuerpo de la respuesta a nuestro modelo. */
public interface SeriesParser {
    Series parse(String body) throws ServiceException;
}
