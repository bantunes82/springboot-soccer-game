package springboot.soccer.game.team.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springboot.soccer.game.team.datatransferobject.TeamDTO;
import springboot.soccer.game.team.domainobject.TeamDO;
import springboot.soccer.game.team.exception.EntityNotFoundException;
import springboot.soccer.game.team.resource.mapper.TeamMapper;
import springboot.soccer.game.team.service.TeamService;
import springboot.soccer.game.team.util.CountryCode;
import springboot.soccer.game.team.util.Range;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Slf4j
@Validated
@RestController
@RequestMapping(value = "/v1/teams", produces = APPLICATION_JSON_VALUE)
public class TeamResource {

    private TeamService teamService;
    private TeamMapper teamMapper;

    @Autowired
    public TeamResource(TeamService teamService, TeamMapper teamMapper) {
        this.teamService = teamService;
        this.teamMapper = teamMapper;
    }

    @GetMapping(path = "/random")
    public ResponseEntity<TeamDTO> findRandomTeam() throws EntityNotFoundException {
        TeamDO teamRandom = teamService.findRandom();

        return ResponseEntity.ok(teamMapper.toTeamDTO(teamRandom));
    }

    @GetMapping(path = "/name/{name}")
    public ResponseEntity<List<TeamDTO>> findTeamByName(@PathVariable("name") String name) {
        List<TeamDO> teams = teamService.findByName(name);

        return ResponseEntity.ok(teamMapper.toTeamDTOList(teams));
    }

    @GetMapping(path = "/country/{countryCode}")
    public ResponseEntity<List<TeamDTO>> findTeamByCountryCode(@PathVariable("countryCode") @CountryCode String countryCode,
                                                               @RequestParam(value = "pageIndex", defaultValue = "0") int pageIndex,
                                                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        List<TeamDO> teams = teamService.findByCountryCode(countryCode, pageIndex, pageSize);

        return ResponseEntity.ok(teamMapper.toTeamDTOList(teams));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity createTeam(@Valid @RequestBody TeamDTO teamDTO) {
        TeamDO teamDO = teamMapper.toTeamDO(teamDTO);
        TeamDO teamSaved = teamService.create(teamDO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(teamSaved.getId())
                .toUri();

        log.debug("New team created with URI %s", location);

        return ResponseEntity.created(location).build();
    }

    @PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamDTO> updateTeam(@PathVariable("id") Long teamId,
                                              @Valid @RequestBody TeamDTO teamDTO) throws EntityNotFoundException {
        TeamDO teamDO = teamMapper.toTeamDO(teamDTO);
        TeamDO teamUpdated = teamService.update(teamId, teamDO);

        return ResponseEntity.ok(teamMapper.toTeamDTO(teamUpdated));
    }

    @PatchMapping(path = "/{id}/level/{value}")
    public ResponseEntity<TeamDTO> updateTeamLevel(@PathVariable("id") Long teamId,
                                                   @Range(min = 1.0, max = 10.0) @PathVariable("value") Double level) throws EntityNotFoundException {
        TeamDO teamUpdated = teamService.updateLevel(teamId, level);

        return ResponseEntity.ok(teamMapper.toTeamDTO(teamUpdated));
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity deleteTeam(@PathVariable("id") Long teamId) throws EntityNotFoundException {
        teamService.delete(teamId);

        return ResponseEntity.noContent().build();
    }
}
