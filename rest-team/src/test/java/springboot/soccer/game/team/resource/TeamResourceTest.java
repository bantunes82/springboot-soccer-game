package springboot.soccer.game.team.resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import springboot.soccer.game.team.datatransferobject.CountryDTO;
import springboot.soccer.game.team.datatransferobject.TeamDTO;
import springboot.soccer.game.team.domainobject.CountryDO;
import springboot.soccer.game.team.domainobject.TeamDO;
import springboot.soccer.game.team.exception.BusinessException;
import springboot.soccer.game.team.resource.mapper.TeamMapper;
import springboot.soccer.game.team.service.TeamService;


import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static springboot.soccer.game.team.exception.BusinessException.ErrorCode.TEAM_NOT_FOUND;


@ExtendWith(MockitoExtension.class)
class TeamResourceTest {

    @Mock
    private TeamService teamService;
    @Mock
    private TeamMapper teamMapper;
    @InjectMocks
    private TeamResource teamResource;

    private CountryDO countryDO;
    private CountryDTO countryDTO;
    private TeamDO teamDO;
    private TeamDTO teamDTO;

    @BeforeEach
    void setUp() {
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
    void findRandomTeam_ReturnsTeam() {
        when(teamService.findRandom()).thenReturn(teamDO);
        when(teamMapper.toTeamDTO(teamDO)).thenReturn(teamDTO);

        ResponseEntity<TeamDTO> response = teamResource.findRandomTeam();

        verify(teamService).findRandom();
        verify(teamMapper).toTeamDTO(teamDO);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals("Sport Club Corinthians Paulista", response.getBody().getName());
    }

    @Test
    void findRandomTeam_GivenThereIsNoTeam_ThrowsEntityNotFoundException() {
        when(teamService.findRandom()).thenThrow(new BusinessException("Could not find any team", TEAM_NOT_FOUND));

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> teamResource.findRandomTeam(), "Could not find any team");
        Assertions.assertEquals(TEAM_NOT_FOUND, exception.getErrorCode());
        verify(teamService).findRandom();
    }

    @Test
    void findTeamByName_GivenValidTeamName_ReturnsTeamList() {
        when(teamService.findByName("Sport Club Corinthians Paulista")).thenReturn(Arrays.asList(teamDO));
        when(teamMapper.toTeamDTOList(Arrays.asList(teamDO))).thenReturn(Arrays.asList(teamDTO));

        ResponseEntity<List<TeamDTO>> response = teamResource.findTeamByName("Sport Club Corinthians Paulista");

        verify(teamService).findByName("Sport Club Corinthians Paulista");
        verify(teamMapper).toTeamDTOList(Arrays.asList(teamDO));
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals(1, response.getBody().size());
        Assertions.assertEquals("Sport Club Corinthians Paulista", response.getBody().get(0).getName());
    }

    @Test
    void findTeamByName_GivenInvalidTeamName_ReturnsEmptyTeamList() {
        when(teamService.findByName("XXX")).thenReturn(Collections.emptyList());
        when(teamMapper.toTeamDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

        ResponseEntity<List<TeamDTO>> response = teamResource.findTeamByName("XXX");

        verify(teamService).findByName("XXX");
        verify(teamMapper).toTeamDTOList(Collections.emptyList());
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals(0, response.getBody().size());
    }

    @Test
    void findTeamByCountryCode_GivenExistentCountryCode_ReturnsTeamList() {
        when(teamService.findByCountryCode("BR", 0, 10)).thenReturn(Arrays.asList(teamDO));
        when(teamMapper.toTeamDTOList(Arrays.asList(teamDO))).thenReturn(Arrays.asList(teamDTO));

        ResponseEntity<List<TeamDTO>> response = teamResource.findTeamByCountryCode("BR", 0, 10);

        verify(teamService).findByCountryCode("BR", 0, 10);
        verify(teamMapper).toTeamDTOList(Arrays.asList(teamDO));
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals(1, response.getBody().size());
        Assertions.assertEquals("Sport Club Corinthians Paulista", response.getBody().get(0).getName());
    }

    @Test
    void findTeamByCountryCode_GivenNoExistentCountryCode_ReturnsEmptyTeamList() {
        when(teamService.findByCountryCode("BR", 0, 10)).thenReturn(Collections.emptyList());
        when(teamMapper.toTeamDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

        ResponseEntity<List<TeamDTO>> response = teamResource.findTeamByCountryCode("BR", 0, 10);

        verify(teamService).findByCountryCode("BR", 0, 10);
        verify(teamMapper).toTeamDTOList(Collections.emptyList());
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals(0, response.getBody().size());
    }

    @Test
    void createTeam_ReturnsTeam() {
        when(teamMapper.toTeamDO(teamDTO)).thenReturn(teamDO);
        when(teamService.create(teamDO)).thenReturn(teamDO);
        HttpServletRequest request = new MockHttpServletRequest("post", "/rest-team/v1/teams/");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ResponseEntity response = teamResource.createTeam(teamDTO);

        verify(teamMapper).toTeamDO(teamDTO);
        verify(teamService).create(teamDO);
        Assertions.assertEquals(201, response.getStatusCodeValue());
        Assertions.assertEquals("/rest-team/v1/teams/1", response.getHeaders().getLocation().getPath());
    }

    @Test
    void updateTeam_GivenValidTeamId_ReturnsTeam() {
        when(teamMapper.toTeamDO(teamDTO)).thenReturn(teamDO);
        when(teamService.update(1L, teamDO)).thenReturn(teamDO);
        when(teamMapper.toTeamDTO(teamDO)).thenReturn(teamDTO);

        ResponseEntity<TeamDTO> response = teamResource.updateTeam(1L, teamDTO);

        verify(teamMapper).toTeamDO(teamDTO);
        verify(teamService).update(1L, teamDO);
        verify(teamMapper).toTeamDTO(teamDO);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals("Sport Club Corinthians Paulista", response.getBody().getName());
    }

    @Test
    void updateTeam_GivenInvalidTeamId_ThrowsEntityNotFoundException() {
        when(teamMapper.toTeamDO(teamDTO)).thenReturn(teamDO);
        when(teamService.update(1L, teamDO)).thenThrow(new BusinessException("Could not find team with id: 1", TEAM_NOT_FOUND));

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> teamResource.updateTeam(1L, teamDTO), "Could not find team with id: 1");
        Assertions.assertEquals(TEAM_NOT_FOUND, exception.getErrorCode());
        verify(teamMapper).toTeamDO(teamDTO);
        verify(teamService).update(1L, teamDO);
    }

    @Test
    void updateTeamLevel_GivenValidTeamId_ReturnsTeam() {
        when(teamService.updateLevel(1L, 8d)).thenReturn(teamDO);
        when(teamMapper.toTeamDTO(teamDO)).thenReturn(teamDTO);

        ResponseEntity<TeamDTO> response = teamResource.updateTeamLevel(1L, 8d);

        verify(teamService).updateLevel(1L, 8d);
        verify(teamMapper).toTeamDTO(teamDO);
        Assertions.assertEquals(200, response.getStatusCodeValue());
        Assertions.assertEquals("Sport Club Corinthians Paulista", response.getBody().getName());
    }

    @Test
    void updateTeamLevel_GivenInvalidTeamId_ThrowsEntityNotFoundException() {
        when(teamService.updateLevel(1L, 8d)).thenThrow(new BusinessException("Could not find team with id: 1", TEAM_NOT_FOUND));

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> teamResource.updateTeamLevel(1L, 8d), "Could not find team with id: 1");
        Assertions.assertEquals(TEAM_NOT_FOUND, exception.getErrorCode());
        verify(teamService).updateLevel(1L, 8d);
    }


    @Test
    void deleteTeam_GivenValidTeamId_DeleteTeam() {
        doNothing().when(teamService).delete(1L);

        ResponseEntity response = teamResource.deleteTeam(1L);

        verify(teamService).delete(1L);
        Assertions.assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void deleteTeam_GivenInvalidTeamId_ThrowsEntityNotFoundException() {
        doThrow(new BusinessException("Could not find team with id: 1", TEAM_NOT_FOUND)).when(teamService).delete(1L);

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> teamResource.deleteTeam(1L), "Could not find team with id: 1");
        Assertions.assertEquals(TEAM_NOT_FOUND, exception.getErrorCode());
        verify(teamService).delete(1L);
    }
}