package springboot.soccer.game.team.datatransferobject;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class TeamDTOTest {

    @Test
    void boilerplate() {
        final Class<?> teamDTO = TeamDTO.class;

        assertPojoMethodsFor(teamDTO)
                .testing(Method.CONSTRUCTOR, Method.GETTER)
                .areWellImplemented();
    }

}