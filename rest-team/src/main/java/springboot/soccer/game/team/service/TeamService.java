package springboot.soccer.game.team.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import springboot.soccer.game.team.dataaccessobject.CountryRepository;
import springboot.soccer.game.team.dataaccessobject.TeamRepository;
import springboot.soccer.game.team.domainobject.CountryDO;
import springboot.soccer.game.team.domainobject.TeamDO;
import springboot.soccer.game.team.exception.EntityNotFoundException;
import springboot.soccer.game.team.util.Range;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class TeamService {

    private TeamRepository teamRepository;
    private CountryRepository countryRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository, CountryRepository countryRepository) {
        this.teamRepository = teamRepository;
        this.countryRepository = countryRepository;
    }

    public TeamDO findRandom() throws EntityNotFoundException {
        return teamRepository.findRandomAndDeletedIsFalse().orElseThrow(() -> new EntityNotFoundException("Could not find any team"));
    }

    public List<TeamDO> findByName(String name) {
        return teamRepository.findByNameAndDeletedIsFalse(name);
    }

    public List<TeamDO> findByCountryCode(String countryCode, int pageIndex, int pageSize) {
        Pageable pageable = PageRequest.of(pageIndex,pageSize, Sort.by("name"));
        return teamRepository.findByCountryDOCodeAndDeletedIsFalse(countryCode, pageable);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TeamDO create(@Valid TeamDO team) {
        Optional<CountryDO> countryDO = countryRepository.findByCode(team.getCountryDO().getCode());
        countryDO.ifPresent(team::setCountryDO);
        return teamRepository.save(team);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TeamDO update(Long teamId, @Valid TeamDO team) throws EntityNotFoundException {
        TeamDO teamSaved = findTeamChecked(teamId);
        Optional<CountryDO> countrySaved = countryRepository.findByCode(team.getCountryDO().getCode());

        updateTeam(team, teamSaved, countrySaved.orElse(team.getCountryDO()));

        return teamSaved;
    }

    private void updateTeam(TeamDO team, TeamDO teamSaved, CountryDO countryDO) {
        team.getNickName().ifPresent(teamSaved::setNickName);
        teamSaved.setLevel(generateNewLevel(team.getLevel(), teamSaved.getLevel()));
        teamSaved.setName(team.getName());
        teamSaved.setFounded(team.getFounded());
        teamSaved.setPicture(team.getPicture());

        teamSaved.setCountryDO(countryDO);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TeamDO updateLevel(Long teamId, @Range(min = 1.0, max = 10.0) Double level) throws EntityNotFoundException {
        TeamDO teamSaved = findTeamChecked(teamId);
        teamSaved.setLevel(generateNewLevel(level, teamSaved.getLevel()));

        return teamSaved;
    }

    private Double generateNewLevel(Double newLevel, Double oldLevel) {
        return Double.sum(newLevel, oldLevel) / 2;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long teamId) throws EntityNotFoundException {
        TeamDO teamDO = findTeamChecked(teamId);
        teamDO.setDeleted(true);
    }


    private TeamDO findTeamChecked(Long teamId) throws EntityNotFoundException {
        return teamRepository.findByIdAndDeletedIsFalse(teamId).orElseThrow(() -> new EntityNotFoundException("Could not find team with id: " + teamId));
    }


}
