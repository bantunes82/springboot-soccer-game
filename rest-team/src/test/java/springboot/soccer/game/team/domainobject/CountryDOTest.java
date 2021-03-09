package springboot.soccer.game.team.domainobject;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class CountryDOTest {

    private final String brCode = "BR";

    @Test
    void boilerplate() {
        final Class<?> countryDO = CountryDO.class;

        assertPojoMethodsFor(countryDO)
                .testing(Method.CONSTRUCTOR, Method.GETTER)
                .areWellImplemented();

    }

    @Test
    void equals_Custom() {
        CountryDO country1 = new CountryDO(1l,"Brasil", brCode);
        CountryDO country2 = new CountryDO(2l,"República Federativa de Brasil", brCode);

        assertEquals(country1, country2);
    }

    @Test
    void equals_CustomWitCodeDifferent() {
        CountryDO country1 = new CountryDO(1l,"Brasil", brCode);
        CountryDO country2 = new CountryDO(1l,"Brasil", "AR");

        assertNotEquals(country1, country2);
    }

    @Test
    void equals_CustomDifferentObjects() {
        CountryDO country1 = new CountryDO(1l,"Brasil", brCode);
        assertNotEquals(country1, new Object());
    }

}