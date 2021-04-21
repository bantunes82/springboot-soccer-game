package springboot.soccer.game.team.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import springboot.soccer.game.team.datatransferobject.ErrorDTO;
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

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "soccer team", description = "Anybody interested in soccer team")
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

    @Operation(summary = "Returns a random soccer team")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "When there is at least one soccer team available", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TeamDTO.class, required = true))),
            @ApiResponse(responseCode = "404", description = "When there is no soccer team available", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDTO.class, required = true)))
    })
   // @Timed(value = "timeFindRandomTeam", description = "Times how long it takes to invoke the findRandomTeam method", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99,0.999})
    @GetMapping(path = "/random")
    public ResponseEntity<TeamDTO> findRandomTeam() throws EntityNotFoundException {
        TeamDO teamRandom = teamService.findRandom();

        return ResponseEntity.ok(teamMapper.toTeamDTO(teamRandom));
    }

    @Operation(summary = "Returns a list of soccer teams that has the specified name")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Returns a list of soccer teams for the specified name", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = TeamDTO.class, required = true)))))
    //@Timed(value = "timeFindTeamByName", description = "Times how long it takes to invoke the findTeamByName method", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99,0.999})
    @GetMapping(path = "/name/{name}")
    public ResponseEntity<List<TeamDTO>> findTeamByName(@Parameter(description = "soccer team name", required = true) @PathVariable("name") String name) {
        List<TeamDO> teams = teamService.findByName(name);

        return ResponseEntity.ok(teamMapper.toTeamDTOList(teams));
    }

    @Operation(summary = "Returns all the soccer teams for the specified Country Code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns a list the soccer teams for the specified Country Code", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = TeamDTO.class, required = true)))),
            @ApiResponse(responseCode = "400", description = "When the countryCode is invalid", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDTO.class, required = true)))
    })
    //@Timed(value = "timeFindTeamByCountryCode", description = "Times how long it takes to invoke the findTeamByCountryCode method", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99,0.999})
    @GetMapping(path = "/country/{countryCode}")
    public ResponseEntity<List<TeamDTO>> findTeamByCountryCode(@Parameter(description = "country code", required = true) @PathVariable("countryCode") @CountryCode String countryCode,
                                                               @Parameter(description = "page index") @RequestParam(value = "pageIndex", defaultValue = "0") int pageIndex,
                                                               @Parameter(description = "page size") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        List<TeamDO> teams = teamService.findByCountryCode(countryCode, pageIndex, pageSize);

        return ResponseEntity.ok(teamMapper.toTeamDTOList(teams));
    }

    @Operation(summary = "Create a soccer team")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The URI of the created soccer team", headers = {@Header(name = LOCATION, description = "URI location of the created soccer team", schema = @Schema(implementation = URI.class))}),
            @ApiResponse(responseCode = "400", description = "When the content of the TeamDTO is invalid", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDTO.class, required = true)))
    })
    //@Timed(value = "timeCreateTeam", description = "Times how long it takes to invoke the createTeam method", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99,0.999})
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createTeam(@io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TeamDTO.class)), description = "soccer team to be created") @Valid @RequestBody TeamDTO teamDTO) {
        TeamDO teamDO = teamMapper.toTeamDO(teamDTO);
        TeamDO teamSaved = teamService.create(teamDO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(teamSaved.getId())
                .toUri();

        log.debug("New team created with URI %s", location);

        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "Update a soccer team for the specified team id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TeamDTO.class, required = true)), description = "Returns the soccer team updated"),
            @ApiResponse(responseCode = "404", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDTO.class, required = true)), description = "When there is no soccer team available for the specified id"),
            @ApiResponse(responseCode = "400", description = "When the content of the TeamDTO is invalid", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDTO.class, required = true)))
    })
    //@Timed(value = "timeUpdateTeam", description = "Times how long it takes to invoke the updateTeam method", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99,0.999})
    @PutMapping(path = "/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamDTO> updateTeam(@Parameter(required = true, description = "soccer team id") @PathVariable("id") Long teamId,
                                              @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TeamDTO.class)), description = "soccer team to be updated") @Valid @RequestBody TeamDTO teamDTO) throws EntityNotFoundException {
        TeamDO teamDO = teamMapper.toTeamDO(teamDTO);
        TeamDO teamUpdated = teamService.update(teamId, teamDO);

        return ResponseEntity.ok(teamMapper.toTeamDTO(teamUpdated));
    }

    @Operation(summary = "Update the soccer team level for the specified team id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TeamDTO.class, required = true)), description = "Returns the soccer team with the updated level"),
            @ApiResponse(responseCode = "404", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDTO.class, required = true)), description = "When there is no soccer team available for the specified id"),
            @ApiResponse(responseCode = "400", description = "When the team level is invalid", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDTO.class, required = true)))
    })
    //@Timed(value = "timeUpdateTeamLevel", description = "Times how long it takes to invoke the updateTeamLevel method", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99,0.999})
    @PatchMapping(path = "/{id}/level/{value}")
    public ResponseEntity<TeamDTO> updateTeamLevel(@Parameter(required = true, description = "soccer team id") @PathVariable("id") Long teamId,
                                                   @Parameter(required = true, description = "soccer team level") @Range(min = 1.0, max = 10.0) @PathVariable("value") Double level) throws EntityNotFoundException {
        TeamDO teamUpdated = teamService.updateLevel(teamId, level);

        return ResponseEntity.ok(teamMapper.toTeamDTO(teamUpdated));
    }

    @Operation(summary = "Delete the soccer team for the specified team id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Soccer Team deleted"),
            @ApiResponse(responseCode = "404", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDTO.class, required = true)), description = "When there is no soccer team available for the specified id"),
    })
    //@Timed(value = "timeDeleteTeam", description = "Times how long it takes to invoke the deleteTeam method", histogram = true, percentiles = {0.5,0.75,0.95,0.98,0.99,0.999})
    @DeleteMapping(path = "{id}")
    public ResponseEntity<Void> deleteTeam(@Parameter(required = true, description = "soccer team id") @PathVariable("id") Long teamId) throws EntityNotFoundException {
        teamService.delete(teamId);

        return ResponseEntity.noContent().build();
    }
}
