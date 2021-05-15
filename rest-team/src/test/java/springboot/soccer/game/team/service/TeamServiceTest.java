package springboot.soccer.game.team.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import springboot.soccer.game.team.dataaccessobject.CountryRepository;
import springboot.soccer.game.team.dataaccessobject.TeamRepository;
import springboot.soccer.game.team.domainobject.CountryDO;
import springboot.soccer.game.team.domainobject.TeamDO;
import springboot.soccer.game.team.exception.BusinessException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private TeamService teamService;

    private TeamDO team1;
    private CountryDO country1;

    @BeforeEach
    void setUp() {
        country1 = new CountryDO(1l, "Brasil", "BR");

        team1 = TeamDO.builder()
                .id(1L)
                .name("Sport Club Corinthians Paulista")
                .nickName("Timao")
                .founded(LocalDate.of(1910, 9, 1))
                .level(9.8)
                .picture("https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/corinthians.jpg")
                .countryDO(country1)
                .build();

    }

    @Test
    void findRandom_ReturnsTeam() {
        when(teamRepository.findRandomAndDeletedIsFalse()).thenReturn(Optional.of(team1));

        TeamDO teamDO = teamService.findRandom();

        verify(teamRepository).findRandomAndDeletedIsFalse();
        Assertions.assertEquals(team1, teamDO);
    }

    @Test
    void findRandom_GivenThereIsNoTeam_ThrowsEntityNotFoundException() {
        when(teamRepository.findRandomAndDeletedIsFalse()).thenReturn(Optional.empty());

        Assertions.assertThrows(BusinessException.class, () -> {
            teamService.findRandom();
        }, "Could not find any team");
        verify(teamRepository).findRandomAndDeletedIsFalse();
    }

    @Test
    void findByName_ReturnsPopulatedOneElement() {
        when(teamRepository.findByNameAndDeletedIsFalse("Sport Club Corinthians Paulista")).thenReturn(Arrays.asList(team1));

        List<TeamDO> teams = teamService.findByName("Sport Club Corinthians Paulista");

        verify(teamRepository).findByNameAndDeletedIsFalse("Sport Club Corinthians Paulista");
        Assertions.assertEquals(1, teams.size());
        Assertions.assertTrue(teams.contains(team1));
    }

    @Test
    void findByName_ReturnsNonePopulatedElement() {
        when(teamRepository.findByNameAndDeletedIsFalse("Sport Club Corinthians Paulista")).thenReturn(Collections.emptyList());

        List<TeamDO> teams = teamService.findByName("Sport Club Corinthians Paulista");

        verify(teamRepository).findByNameAndDeletedIsFalse("Sport Club Corinthians Paulista");
        Assertions.assertEquals(0, teams.size());
    }

    @Test
    void findByCountryCode_ReturnsPopulatedOneElement() {
        Pageable pageable = PageRequest.of(1, 1, Sort.by("name"));
        when(teamRepository.findByCountryDOCodeAndDeletedIsFalse("BR", pageable)).thenReturn(Arrays.asList(team1));

        List<TeamDO> teams = teamService.findByCountryCode("BR", 1, 1);

        verify(teamRepository).findByCountryDOCodeAndDeletedIsFalse("BR", pageable);
        Assertions.assertEquals(1, teams.size());
        Assertions.assertTrue(teams.contains(team1));
    }

    @Test
    void findByCountryCode_ReturnsNonePopulatedElement() {
        Pageable pageable = PageRequest.of(1, 1, Sort.by("name"));
        when(teamRepository.findByCountryDOCodeAndDeletedIsFalse("BR", pageable)).thenReturn(Collections.emptyList());

        List<TeamDO> teams = teamService.findByCountryCode("BR", 1, 1);

        verify(teamRepository).findByCountryDOCodeAndDeletedIsFalse("BR", pageable);
        Assertions.assertEquals(0, teams.size());
    }

    @Test
    void create_ReturnsEntity() {
        when(countryRepository.findByCode("BR")).thenReturn(Optional.of(country1));
        when(teamRepository.save(team1)).thenReturn(team1);

        TeamDO team = teamService.create(team1);

        Assertions.assertEquals(team1, team);
        verify(teamRepository).save(team1);
        verify(countryRepository).findByCode("BR");
    }

    @Test
    void update_GivenExistingTeamId_ReturnsEntity() {
        team1.setId(1000L);
        team1.setNickName("Coringao");
        when(teamRepository.findByIdAndDeletedIsFalse(1L)).thenReturn(Optional.of(team1));

        TeamDO updateTeam = teamService.update(1L, team1);

        verify(teamRepository).findByIdAndDeletedIsFalse(1L);
        Assertions.assertEquals(team1, updateTeam);
    }

    @Test
    void update_GivenNonExistingTeamId_ThrowsEntityNotFoundException() {
        team1.setId(1000L);
        team1.setNickName("Coringao");
        when(teamRepository.findByIdAndDeletedIsFalse(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(BusinessException.class, () -> {
            teamService.update(1L, team1);
        }, "Could not find team with id: 1");
        verify(teamRepository).findByIdAndDeletedIsFalse(1L);
    }

    @Test
    void updateLevel_GivenExistingTeamId_ReturnsCorrectAvg() {
        when(teamRepository.findByIdAndDeletedIsFalse(1L)).thenReturn(Optional.of(team1));

        Double newRate = Double.valueOf(3.5);
        Double expected = Double.sum(team1.getLevel(), newRate) / 2;

        TeamDO updatedTeam = teamService.updateLevel(1L, newRate);

        verify(teamRepository).findByIdAndDeletedIsFalse(1L);
        Assertions.assertEquals(expected, updatedTeam.getLevel());
    }

    @Test
    void updateLevel_GivenNonExistingTeamId_ThrowsEntityNotFoundException() {
        when(teamRepository.findByIdAndDeletedIsFalse(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(BusinessException.class, () -> {
            teamService.updateLevel(1L, 3.5);
        }, "Could not find team with id: 1");
        verify(teamRepository).findByIdAndDeletedIsFalse(1L);
    }

    @Test
    void delete_GivenExistingTeamId_ExecuteSuccessfully() {
        when(teamRepository.findByIdAndDeletedIsFalse(1L)).thenReturn(Optional.of(team1));

        teamService.delete(1L);

        verify(teamRepository).findByIdAndDeletedIsFalse(1L);
        Assertions.assertTrue(team1.getDeleted());
    }

    @Test
    void delete_GivenNonExistingTeamId_ThrowsEntityNotFoundException() {
        when(teamRepository.findByIdAndDeletedIsFalse(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(BusinessException.class, () -> {
            teamService.delete(1L);
        }, "Could not find team with id: 1");
        verify(teamRepository).findByIdAndDeletedIsFalse(1L);
    }
}