package springboot.soccer.game.team.domainobject;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class BaseDOTest {

    @Test
    void boilerplate() {
        final Class<?> baseDO = BaseDO.class;

        assertPojoMethodsFor(baseDO)
                .testing(Method.CONSTRUCTOR, Method.GETTER)
                .areWellImplemented();

    }

}