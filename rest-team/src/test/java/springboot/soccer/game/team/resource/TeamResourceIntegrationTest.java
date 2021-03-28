package springboot.soccer.game.team.resource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import springboot.soccer.game.team.container.Containers;
import springboot.soccer.game.team.datatransferobject.CountryDTO;
import springboot.soccer.game.team.datatransferobject.TeamDTO;

import java.time.LocalDate;
import java.util.Locale;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static springboot.soccer.game.team.constants.Validation.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TeamResourceIntegrationTest extends Containers {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final Locale SPAIN_LOCALE = Locale.forLanguageTag("ES");
    private static final String TEAMS_JSON = "[{\"name\":\"Sport Club Corinthians Paulista\",\"nickName\":\"Timao\",\"founded\":\"1910-09-01\",\"level\":8.0,\"picture\":\"https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/corinthians.jpg\",\"countryDTO\":{\"name\":\"Brazil\",\"code\":\"BR\"}}]";
    private static final String CORINTHIANS_TEAM_JSON = "{\"name\":\"Sport Club Corinthians Paulista\",\"nickName\":\"Timao\",\"founded\":\"1910-09-01\",\"level\":8.0,\"picture\":\"https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/corinthians.jpg\",\"countryDTO\":{\"name\":\"Brazil\",\"code\":\"BR\"}}";
    private static final String BAYERN_JSON = "{\"name\":\"FC Bayern de Munchen\",\"nickName\":\"Bayer\",\"founded\":\"1900-02-27\",\"level\":8.55,\"picture\":\"https://storage.googleapis.com/www-paredro-com/uploads/2019/02/%E2%96%B7-Esta-es-la-historia-del-logo-del-Bayern-Mu%CC%81nich-el-gigante-de-Baviera.jpg\",\"countryDTO\":{\"name\":\"Germany\",\"code\":\"DE\"}}";
    private static String TEAM_PATH = "/v1/teams/";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MessageSource messageSource;
    private String bayernMunchen;
    private String invalidTeamDTO;
    private String invalidTeamDTONullValues;
    private String invalidTeamDTONullCountryDTO;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        createBayerMunchenTeam();
        createInvalidTeamDTO();
        createInvalidTeamDTONullValues();
        createInvalidTeamDTONullCountryDTO();

    }

    @Test
    void findRandomTeam_GivenThereIsTeam_ReturnsOK() throws Exception {
        mockMvc.perform(get(TEAM_PATH + "/random"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(CORINTHIANS_TEAM_JSON));
    }


    @Test
    void findTeamByName_GivenNoExistentTeamName_ReturnsEmptyTeamList() throws Exception {
        mockMvc.perform(get(TEAM_PATH + "/name/XXXX"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    void findTeamByName_GivenExistentTeamName_ReturnsTeamListWithOneElement() throws Exception {
        mockMvc.perform(get(TEAM_PATH + "/name/Sport Club Corinthians Paulista"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(TEAMS_JSON));
    }


    @Test
    void findTeamByCountryCode_GivenNoExistentTeamWithTheCountryCode_ReturnsEmptyTeamList() throws Exception {
        mockMvc.perform(get(TEAM_PATH + "/country/AR"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    void findTeamByCountryCode_GivenInvalidCountryCode_ReturnsBadRequest() throws Exception {
        String errorMessage = getErrorMessage(COUNTRY_CODE_INVALID, Locale.ENGLISH);

        mockMvc.perform(get(TEAM_PATH + "/country/XX"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"errors\":{\"findTeamByCountryCode.countryCode\":\"" + errorMessage + "\"}}"));
    }

    @Test
    void findTeamByCountryCode_GivenInvalidCountryCode_ReturnsBadRequestSpanish() throws Exception {
        String errorMessage = getErrorMessage(COUNTRY_CODE_INVALID, SPAIN_LOCALE);

        mockMvc.perform(get(TEAM_PATH + "/country/XX")
                .header("Accept-Language", "es"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"errors\":{\"findTeamByCountryCode.countryCode\":\"" + errorMessage + "\"}}"));

    }

    @Test
    void findTeamByCountryCode_GivenExistentTeamWithTheCountryCode_ReturnsTeamListWithOneElement() throws Exception {
        mockMvc.perform(get(TEAM_PATH + "/country/BR"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(TEAMS_JSON));
    }


    @Test
    void updateTeam_GivenInvalidDTOValues_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put(TEAM_PATH + "-1")
                .contentType(APPLICATION_JSON)
                .content(invalidTeamDTO)
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"errors\":{\"countryDTO.name\":\"" + getErrorMessage(COUNTRY_NAME_SIZE, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"countryDTO.code\":\"" + getErrorMessage(COUNTRY_CODE_INVALID, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"founded\":\"" + getErrorMessage(TEAM_FOUNDED_PAST, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"level\":\"" + getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"name\":\"" + getErrorMessage(TEAM_NAME_SIZE, Locale.ENGLISH) + "\"}}"));
    }

    @Test
    void updateTeam_GivenInvalidDTONullValues_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put(TEAM_PATH + "-1")
                .contentType(APPLICATION_JSON)
                .content(invalidTeamDTONullValues)
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"errors\":{\"countryDTO.name\":\"" + getErrorMessage(COUNTRY_NAME_BLANK, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"countryDTO.code\":\"" + getErrorMessage(COUNTRY_CODE_INVALID, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"picture\":\"" + getErrorMessage(TEAM_PICTURE_BLANK, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"founded\":\"" + getErrorMessage(TEAM_FOUNDED_BLANK, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"level\":\"" + getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"name\":\"" + getErrorMessage(TEAM_NAME_BLANK, Locale.ENGLISH) + "\"}}"));
    }


    @Test
    void updateTeam_GivenInvalidDTONullCountryDTO_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put(TEAM_PATH + "-1")
                .contentType(APPLICATION_JSON)
                .content(invalidTeamDTONullCountryDTO)
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"errors\":{\"countryDTO\":\"" + getErrorMessage(TEAM_COUNTRY_NULL, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"picture\":\"" + getErrorMessage(TEAM_PICTURE_BLANK, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"founded\":\"" + getErrorMessage(TEAM_FOUNDED_BLANK, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"level\":\"" + getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH) + "\"}}"))
                .andExpect(content().json("{\"errors\":{\"name\":\"" + getErrorMessage(TEAM_NAME_BLANK, Locale.ENGLISH) + "\"}}"));
    }


    @Test
    void updateTeam_GivenInvalidTeamId_ReturnsNotFound() throws Exception {
        mockMvc.perform(put(TEAM_PATH + "-1000")
                .contentType(APPLICATION_JSON)
                .content(bayernMunchen)
        )
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"errors\":{\"error\":\"" + getErrorMessage(TEAM_NOT_FOUND, new Object[]{-1000}, Locale.ENGLISH) + "\"}}"));
    }


    @Test
    void updateTeam_GivenValidDTO_ReturnsOK() throws Exception {
        mockMvc.perform(put(TEAM_PATH + "-1")
                .contentType(APPLICATION_JSON)
                .content(bayernMunchen)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(BAYERN_JSON));
    }


    @Test
    void updateTeamLevel_GivenLowerRangeLevelNotAllowed_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch(TEAM_PATH + "-1/level/0.9"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"errors\":{\"updateTeamLevel.level\":\"" + getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH) + "\"}}"));
    }

    @Test
    void updateTeamLevel_GivenUpperRangeLevelNotAllowed_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch(TEAM_PATH + "-1/level/10.1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"errors\":{\"updateTeamLevel.level\":\"" + getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH) + "\"}}"));
    }

    @Test
    void updateTeamLevel_GivenInvalidTeamId_ReturnsNotFound() throws Exception {
        mockMvc.perform(patch(TEAM_PATH + "-1000/level/8"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"errors\":{\"error\":\"" + getErrorMessage(TEAM_NOT_FOUND, new Object[]{-1000}, Locale.ENGLISH) + "\"}}"));
    }

    @Test
    void updateTeamLevel_GivenValidRangeLevel_ReturnsOK() throws Exception {
        mockMvc.perform(patch(TEAM_PATH + "-1/level/8.8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json("{\"level\":8.4}"));
    }

    private String getErrorMessage(String messageKeyWithBraces, Locale locale) {
        return getErrorMessage(messageKeyWithBraces, null, locale);
    }

    private String getErrorMessage(String messageKeyWithBraces, Object[] args, Locale locale) {
        return messageSource.getMessage(messageKeyWithBraces.replaceAll("[{}]", ""), args, locale);
    }


    private void createBayerMunchenTeam() throws JsonProcessingException {
        CountryDTO germany = new CountryDTO("Germany", "DE");
        TeamDTO teamDTO = TeamDTO.builder()
                .countryDTO(germany)
                .founded(LocalDate.of(1900, 2, 27))
                .level(9.1d)
                .name("FC Bayern de Munchen")
                .picture("https://storage.googleapis.com/www-paredro-com/uploads/2019/02/%E2%96%B7-Esta-es-la-historia-del-logo-del-Bayern-Mu%CC%81nich-el-gigante-de-Baviera.jpg")
                .nickName("Bayer")
                .build();

        bayernMunchen = MAPPER.writeValueAsString(teamDTO);
    }

    private void createInvalidTeamDTO() throws JsonProcessingException {
        CountryDTO invalidCountryDTO = new CountryDTO("XX", "XX");
        TeamDTO teamDTO = TeamDTO.builder()
                .countryDTO(invalidCountryDTO)
                .founded(LocalDate.now())
                .level(11d)
                .name("ss")
                .picture("picture")
                .nickName("nickname")
                .build();

        invalidTeamDTO = MAPPER.writeValueAsString(teamDTO);
    }

    private void createInvalidTeamDTONullValues() throws JsonProcessingException {
        CountryDTO invalidCountryDTONullValues = new CountryDTO(null, null);
        TeamDTO teamDTO = TeamDTO.builder()
                .countryDTO(invalidCountryDTONullValues)
                .build();

        invalidTeamDTONullValues = MAPPER.writeValueAsString(teamDTO);
    }

    private void createInvalidTeamDTONullCountryDTO() throws JsonProcessingException {
        invalidTeamDTONullCountryDTO = MAPPER.writeValueAsString(TeamDTO.builder().build());
    }
}
