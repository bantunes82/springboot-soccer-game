package springboot.soccer.game.team.resource.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import springboot.soccer.game.team.datatransferobject.CountryDTO;
import springboot.soccer.game.team.domainobject.CountryDO;

@Mapper(config = SpringMappingConfig.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CountryMapper {

    CountryDO toCountryDO(CountryDTO countryDTO);

    CountryDTO toCountryDTO(CountryDO countryDO);

}
