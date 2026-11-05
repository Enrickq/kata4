package software.ulpgc.katas.infrastructure.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import software.ulpgc.katas.architecture.model.Series;
import software.ulpgc.katas.architecture.ports.SeriesParser;
import software.ulpgc.katas.architecture.ports.ServiceException;

import java.util.ArrayList;
import java.util.List;

/** Parser limitado y explícito para {"values":[0,1,1,...]}. */
public final class JsonSeriesParser implements SeriesParser {
    @Override
    public Series parse(String body) throws ServiceException {
        if (body == null || body.isBlank()) {
            throw ServiceException.invalidResponse(null);
        }
        try {
            JsonElement root = JsonParser.parseString(body);
            if (!root.isJsonObject() || !root.getAsJsonObject().has("values")
                    || !root.getAsJsonObject().get("values").isJsonArray()) {
                throw ServiceException.invalidResponse(null);
            }
            JsonArray numbers = root.getAsJsonObject().getAsJsonArray("values");
            if (numbers.size() == 0) {
                throw ServiceException.invalidResponse(null);
            }
            List<Integer> values = new ArrayList<>();
            for (JsonElement element : numbers) {
                if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
                    throw ServiceException.invalidResponse(null);
                }
                values.add(element.getAsBigDecimal().intValueExact());
            }
            return new Series(values);
        } catch (JsonParseException | IllegalStateException | ArithmeticException e) {
            throw ServiceException.invalidResponse(e);
        }
    }
}
