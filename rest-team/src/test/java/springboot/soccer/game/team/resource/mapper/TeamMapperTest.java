package springboot.soccer.game.team.resource.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import springboot.soccer.game.team.datatransferobject.CountryDTO;
import springboot.soccer.game.team.datatransferobject.TeamDTO;
import springboot.soccer.game.team.domainobject.CountryDO;
import springboot.soccer.game.team.domainobject.TeamDO;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisabledInNativeImage  // <!-- Mockito issue with Native Image https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-with-GraalVM#testing-->
class TeamMapperTest {

    private CountryDO countryDO;
    private CountryDTO countryDTO;
    private TeamDO teamDO;
    private TeamDTO teamDTO;
    @Mock
    private CountryMapper countryMapper;
    private TeamMapper teamMapper;

    @BeforeEach
    void setUp() {
        teamMapper = new TeamMapperImpl(countryMapper);

        countryDO = new CountryDO(1l, "Brasil", "BR");
        countryDTO = new CountryDTO("Brasil", "BR");

        teamDO = TeamDO.builder()
                .id(1L)
                .name("Sport Club Corinthians Paulista")
                .nickName("Timao")
                .founded(LocalDate.of(1910, 9, 1))
                .level(9.8)
                .picture("https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/corinthians.jpg")
                .countryDO(countryDO)
                .deleted(false)
                .build();

        teamDTO = TeamDTO.builder()
                .name("Sport Club Corinthians Paulista")
                .nickName("Timao")
                .founded(LocalDate.of(1910, 9, 1))
                .level(9.8)
                .picture("https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/corinthians.jpg")
                .countryDTO(countryDTO)
                .build();
    }

    @Test
    void toTeamDO_ReturnsTeamDO() {
        when(countryMapper.toCountryDO(countryDTO)).thenReturn(countryDO);

        TeamDO result = teamMapper.toTeamDO(teamDTO);

        Assertions.assertEquals(teamDTO.name(), result.getName());
        Assertions.assertEquals(teamDTO.nickName(), result.getNickName().get());
        Assertions.assertEquals(teamDTO.picture(), result.getPicture());
        Assertions.assertEquals(teamDTO.level(), result.getLevel());
        Assertions.assertEquals(teamDTO.founded(), result.getFounded());
        Assertions.assertEquals(teamDTO.countryDTO().code(), result.getCountryDO().getCode());
        Assertions.assertEquals(teamDTO.countryDTO().name(), result.getCountryDO().getName());
    }

    @Test
    void toTeamDO_GivenNullValue_ReturnsNullValue() {
        TeamDO result = teamMapper.toTeamDO(null);

        Assertions.assertNull(result);
    }

    @Test
    void toTeamDTO_ReturnsTeamDTO() {
        when(countryMapper.toCountryDTO(countryDO)).thenReturn(countryDTO);

        TeamDTO result = teamMapper.toTeamDTO(teamDO);

        Assertions.assertEquals(teamDO.getName(), result.name());
        Assertions.assertEquals(teamDO.getNickName().get(), result.nickName());
        Assertions.assertEquals(teamDO.getPicture(), result.picture());
        Assertions.assertEquals(teamDO.getLevel(), result.level());
        Assertions.assertEquals(teamDO.getFounded(), result.founded());
        Assertions.assertEquals(teamDO.getCountryDO().getCode(), result.countryDTO().code());
        Assertions.assertEquals(teamDO.getCountryDO().getName(), result.countryDTO().name());
    }

    @Test
    void toTeamDTO_GivenNullValue_ReturnsNullValue() {
        TeamDTO result = teamMapper.toTeamDTO(null);

        Assertions.assertNull(result);
    }

    @Test
    void toTeamDTOList_ReturnsListTeamDTO() {
        when(countryMapper.toCountryDTO(countryDO)).thenReturn(countryDTO);

        List<TeamDTO> results = teamMapper.toTeamDTOs(Arrays.asList(teamDO));
        TeamDTO result = results.get(0);

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(teamDO.getName(), result.name());
        Assertions.assertEquals(teamDO.getNickName().get(), result.nickName());
        Assertions.assertEquals(teamDO.getPicture(), result.picture());
        Assertions.assertEquals(teamDO.getLevel(), result.level());
        Assertions.assertEquals(teamDO.getFounded(), result.founded());
        Assertions.assertEquals(teamDO.getCountryDO().getCode(), result.countryDTO().code());
        Assertions.assertEquals(teamDO.getCountryDO().getName(), result.countryDTO().name());
    }

    @Test
    void toTeamDTOList_GivenNullValue_ReturnsNullValue() {
        List<TeamDTO> results = teamMapper.toTeamDTOs(null);

        Assertions.assertNull(results);
    }
}