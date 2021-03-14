package springboot.soccer.game.team.datatransferobject;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class CountryDTOTest {

    @Test
    void boilerplate() {
        final Class<?> countryDTO = CountryDTO.class;

        assertPojoMethodsFor(countryDTO)
                .testing(Method.CONSTRUCTOR, Method.GETTER)
                .areWellImplemented();
    }

}