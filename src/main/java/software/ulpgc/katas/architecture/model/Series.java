package software.ulpgc.katas.architecture.model;

import java.util.List;

/** Modelo pasivo: los valores devueltos por el servicio. */
public record Series(List<Integer> values) {
    public Series {
        values = List.copyOf(values);
    }
}
