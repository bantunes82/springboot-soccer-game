package springboot.soccer.game.team.datatransferobject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class ErrorDTOTest {
    @Test
    void testErrorDTONullValueInTheConstructor() {
        Assertions.assertEquals(new ErrorDTO(Collections.emptyMap()), new ErrorDTO(null));
    }

}