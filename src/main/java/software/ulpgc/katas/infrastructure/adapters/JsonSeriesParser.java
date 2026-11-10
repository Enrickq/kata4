package software.ulpgc.katas.infrastructure.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import software.ulpgc.katas.architecture.model.Series;
import software.ulpgc.katas.architecture.ports.SeriesParser;
import software.ulpgc.katas.architecture.ports.ServiceException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/** Parser limitado y explícito para {"values":[0,1,1,...]}. */
public final class JsonSeriesParser implements SeriesParser {
    @Override
    public Series parse(String body) throws ServiceException {
        if (body == null || body.isBlank()) {
            throw ServiceException.invalidResponse(null);
        }
        try (JsonReader reader = new JsonReader(new StringReader(body))) {
            reader.setStrictness(Strictness.STRICT);
            JsonElement root = JsonParser.parseReader(reader);
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw ServiceException.invalidResponse(null);
            }
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
        } catch (IOException | JsonParseException | IllegalStateException
                 | ArithmeticException | NumberFormatException e) {
            throw ServiceException.invalidResponse(e);
        }
    }
}
