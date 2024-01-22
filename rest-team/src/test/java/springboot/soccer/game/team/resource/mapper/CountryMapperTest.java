package springboot.soccer.game.team.resource.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import springboot.soccer.game.team.datatransferobject.CountryDTO;
import springboot.soccer.game.team.domainobject.CountryDO;

class CountryMapperTest {

    private CountryMapper countryMapper;
    private CountryDO countryDO;
    private CountryDTO countryDTO;

    @BeforeEach
    void setUp() {
        countryMapper = Mappers.getMapper(CountryMapper.class);

        countryDO = new CountryDO(1L, "Brasil", "BR");
        countryDTO = new CountryDTO("Brasil", "BR");
    }

    @Test
    void toCountryDO_ReturnsCountryDO() {
        CountryDO result = countryMapper.toCountryDO(countryDTO);

        Assertions.assertEquals(countryDTO.name(), result.getName());
        Assertions.assertEquals(countryDTO.code(), result.getCode());
    }

    @Test
    void toCountryDO_GivenNullValue_ReturnsNullValue() {
        CountryDO result = countryMapper.toCountryDO(null);

        Assertions.assertNull(result);
    }

    @Test
    void toCountryDTO_ReturnsCountryDTO() {
        CountryDTO result = countryMapper.toCountryDTO(countryDO);

        Assertions.assertEquals(countryDO.getName(), result.name());
        Assertions.assertEquals(countryDO.getCode(), result.code());
    }

    @Test
    void toCountryDTO_GivenNullValue_ReturnsNullValue() {
        CountryDTO result = countryMapper.toCountryDTO(null);

        Assertions.assertNull(result);
    }
}