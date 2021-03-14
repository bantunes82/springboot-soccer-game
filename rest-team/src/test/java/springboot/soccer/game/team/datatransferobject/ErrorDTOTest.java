package springboot.soccer.game.team.datatransferobject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class ErrorDTOTest {

    @Test
    void boilerplate() {
        final Class<?> errorDTO = ErrorDTO.class;

        assertPojoMethodsFor(errorDTO)
                .testing(Method.GETTER)
                .areWellImplemented();
    }

    @Test
    void testErrorDTONullValueInTheConstructor() {
        Assertions.assertThrows(NullPointerException.class, () -> new ErrorDTO(null), "errors is marked non-null but is null");
    }

}