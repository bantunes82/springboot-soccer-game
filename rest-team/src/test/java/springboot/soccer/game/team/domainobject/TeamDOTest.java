package springboot.soccer.game.team.domainobject;


import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class TeamDOTest {

    private final String teamName = "Sport Club Corinthians Paulista";
    private final CountryDO countryDO = new CountryDO(1l,"Brasil", "BR");

    private final TeamDO team1 = TeamDO.builder()
            .id(1L)
            .name(teamName)
            .nickName("Timao")
            .founded(LocalDate.of(1910, 9, 1))
            .level(9.8)
            .picture("https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/corinthians.jpg")
            .countryDO(countryDO)
            .build();

    private final TeamDO team2 = TeamDO.builder()
            .id(2L)
            .name(teamName)
            .nickName("Time do Povo")
            .founded(LocalDate.of(1910, 9, 2))
            .level(8.2)
            .picture("https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/cruzeiro.jpg")
            .countryDO(countryDO)
            .build();


    @Test
    void boilerplate() {
        final Class<?> teamDO = TeamDO.class;

        assertPojoMethodsFor(teamDO)
                .testing(Method.CONSTRUCTOR, Method.SETTER)
                .areWellImplemented();
    }

    @Test
    void equals_Custom() {
        assertEquals(team1, team2);
    }

    @Test
    void equals_CustomWithNameDifferent() {
        team2.setName("Cruzeiro");
        assertNotEquals(team1, team2);
    }


    @Test
    void equals_CustomWithCountryDifferent() {
        team2.setCountryDO(new CountryDO(2l,"Argentina", "AR"));
        assertNotEquals(team1, team2);
    }

    @Test
    void equals_CustomDifferentObjects() {
        assertNotEquals(team1, new Object());
    }
}