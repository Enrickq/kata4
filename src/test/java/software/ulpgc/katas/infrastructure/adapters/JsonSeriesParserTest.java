package software.ulpgc.katas.infrastructure.adapters;

import org.junit.jupiter.api.Test;
import software.ulpgc.katas.architecture.ports.ServiceException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonSeriesParserTest {
    private final JsonSeriesParser parser = new JsonSeriesParser();

    @Test
    void convertsJsonIntoImmutableModel() throws Exception {
        var series = parser.parse("{\"values\":[0,1,1,2,3]}");
        assertEquals(List.of(0, 1, 1, 2, 3), series.values());
        assertThrows(UnsupportedOperationException.class, () -> series.values().add(5));
    }

    @Test
    void rejectsEmptyBody() {
        assertInvalid("");
        assertInvalid("   ");
    }

    @Test
    void rejectsMalformedJsonAndMissingField() {
        assertInvalid("{broken}");
        assertInvalid("{}");
        assertInvalid("{\"values\":[]}");
        assertInvalid("{\"values\":null}");
    }

    @Test
    void rejectsNonIntegerValues() {
        assertInvalid("{\"values\":[0,\"1\"]}");
        assertInvalid("{\"values\":[0,1.5]}");
        assertInvalid("{\"values\":[999999999999]}");
    }

    @Test
    void rejectsNonStandardJsonSyntax() {
        assertInvalid("{values:[0,1]}");
        assertInvalid("{'values':[0,1]}");
        assertInvalid("/* comentario */ {\"values\":[0,1]}");
        assertInvalid("{\"values\":[0,1]} // comentario");
    }

    @Test
    void rejectsContentAfterTheJsonDocument() {
        assertInvalid("{\"values\":[0,1]} {\"values\":[2,3]}");
        assertInvalid("{\"values\":[0,1]} contenido adicional");
    }

    @Test
    void rejectsExtremeNumbersAsInvalidResponses() {
        assertInvalid("{\"values\":[1e10000]}");
        assertInvalid("{\"values\":[1e2147483648]}");
    }

    private void assertInvalid(String json) {
        ServiceException error = assertThrows(ServiceException.class, () -> parser.parse(json));
        assertEquals(ServiceException.Kind.INVALID_RESPONSE, error.kind());
    }
}
