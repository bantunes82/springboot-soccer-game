package springboot.soccer.game.team.datatransferobject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ErrorDTOTest {
    @Test
    void testErrorDTONullValueInTheConstructor() {
        Assertions.assertThrows(NullPointerException.class, () -> new ErrorDTO(null), "errors is marked non-null but is null");
    }

}