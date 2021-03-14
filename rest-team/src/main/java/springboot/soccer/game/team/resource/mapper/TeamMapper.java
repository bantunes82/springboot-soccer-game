package springboot.soccer.game.team.resource.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import springboot.soccer.game.team.datatransferobject.TeamDTO;
import springboot.soccer.game.team.domainobject.TeamDO;

import java.util.List;
import java.util.Optional;

@Mapper(config = SpringMappingConfig.class, uses = {CountryMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TeamMapper {

    @Mapping(source = "countryDTO", target = "countryDO")
    TeamDO toTeamDO(TeamDTO teamDTO);

    default String map(Optional<String> value){
        return value.orElse("");
    }

    @InheritInverseConfiguration
    TeamDTO toTeamDTO(TeamDO teamDO);

    List<TeamDTO> toTeamDTOList(List<TeamDO> teamDOs);

}