package springboot.soccer.game.team.resource;

import me.escoffier.loom.loomunit.LoomUnitExtension;
import me.escoffier.loom.loomunit.ShouldNotPin;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import springboot.soccer.game.team.auth.AccessToken;
import springboot.soccer.game.team.config.TestContainersConfiguration;
import springboot.soccer.game.team.datatransferobject.CountryDTO;
import springboot.soccer.game.team.datatransferobject.ErrorDTO;
import springboot.soccer.game.team.datatransferobject.TeamDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static springboot.soccer.game.team.validation.ConstraintMessage.*;
import static springboot.soccer.game.team.exception.BusinessException.ErrorCode.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfiguration.class)
@Tag("integration")
@ExtendWith(LoomUnitExtension.class)
@ShouldNotPin
class TeamResourceIntegrationTest {

    private static final String ES = "ES";
    private static final Locale SPAIN_LOCALE = Locale.forLanguageTag(ES);
    private static final String TEAM_RESOURCE_PATH = "/v1/teams";

    private final TestRestTemplate testRestTemplate;
    private final MessageSource messageSource;
    private final String authServerUrl;
    private final String clientId;
    private TeamDTO bayernMunchen;
    private TeamDTO bayerLeverkusen;
    private TeamDTO corinthians;
    private TeamDTO invalidTeamDTO;
    private TeamDTO invalidTeamDTONullValues;
    private TeamDTO invalidTeamDTONullCountryDTO;
    private HttpHeaders headers;

    @Autowired
    public TeamResourceIntegrationTest(TestRestTemplate testRestTemplate, MessageSource messageSource,
                                       @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String authServerUrl,
                                       @Value("${keycloak.resource}") String clientId) {
        this.testRestTemplate = testRestTemplate;
        this.messageSource = messageSource;
        this.authServerUrl = authServerUrl;
        this.clientId = clientId;
    }

    @BeforeEach
    void setUp() {
        createCorinthiansTeam();
        createBayerMunchenTeam();
        createBayerLeverkusen();
        createInvalidTeamDTO();
        createInvalidTeamDTONullValues();
        createInvalidTeamDTONullCountryDTO();
        headers = new HttpHeaders();

    }

    @Test
    void findRandomTeam_GivenThereIsTeam_ReturnsOK() {
        ResponseEntity<TeamDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/random", HttpMethod.GET, new HttpEntity<>(headers), TeamDTO.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void findTeamByName_GivenNoExistentTeamName_ReturnsEmptyTeamList() {
        ParameterizedTypeReference<List<TeamDTO>> paramType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<List<TeamDTO>> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/name/XXXX", HttpMethod.GET, new HttpEntity<>(headers), paramType);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(0, response.getBody().size());
    }

    @Test
    void findTeamByName_GivenExistentTeamName_ReturnsTeamListWithOneElement() {
        ParameterizedTypeReference<List<TeamDTO>> paramType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<List<TeamDTO>> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/name/Sport Club Corinthians Paulista", HttpMethod.GET, new HttpEntity<>(headers), paramType);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(1, response.getBody().size());
        Assertions.assertEquals("Sport Club Corinthians Paulista", response.getBody().get(0).name());
        Assertions.assertEquals("BR", response.getBody().get(0).countryDTO().code());
    }

    @Test
    void findTeamByCountryCode_GivenNoExistentTeamWithTheCountryCode_ReturnsEmptyTeamList() {
        ParameterizedTypeReference<List<TeamDTO>> paramType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<List<TeamDTO>> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/country/CL", HttpMethod.GET, new HttpEntity<>(headers), paramType);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(0, response.getBody().size());
    }

    @Test
    void findTeamByCountryCode_GivenInvalidCountryCode_ReturnsBadRequest() {
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/country/XX", HttpMethod.GET, new HttpEntity<>(headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(COUNTRY_CODE_INVALID, Locale.ENGLISH), response.getBody().errors().get("findTeamByCountryCode.countryCode"));
    }

    @Test
    void findTeamByCountryCode_GivenInvalidCountryCode_ReturnsBadRequest_Spanish() {
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/country/XX", HttpMethod.GET, new HttpEntity<>(headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(COUNTRY_CODE_INVALID, SPAIN_LOCALE), response.getBody().errors().get("findTeamByCountryCode.countryCode"));
    }

    @Test
    void findTeamByCountryCode_GivenExistentTeamWithTheCountryCode_ReturnsTeamListWithOneElement() {
        ParameterizedTypeReference<List<TeamDTO>> paramType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<List<TeamDTO>> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/country/CO", HttpMethod.GET, new HttpEntity<>(headers), paramType);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(1, response.getBody().size());
        Assertions.assertEquals("América de Cali S.A", response.getBody().get(0).name());
        Assertions.assertEquals("CO", response.getBody().get(0).countryDTO().code());
    }

    @Test
    void updateTeam_GivenInvalidDTOValues_ReturnsBadRequest() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-2", HttpMethod.PUT, new HttpEntity<>(invalidTeamDTO, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(COUNTRY_NAME_SIZE, Locale.ENGLISH), response.getBody().errors().get("countryDTO.name"));
        Assertions.assertEquals(getErrorMessage(COUNTRY_CODE_INVALID, Locale.ENGLISH), response.getBody().errors().get("countryDTO.code"));
        Assertions.assertEquals(getErrorMessage(TEAM_FOUNDED_PAST, Locale.ENGLISH), response.getBody().errors().get("founded"));
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH), response.getBody().errors().get("level"));
        Assertions.assertEquals(getErrorMessage(TEAM_NAME_SIZE, Locale.ENGLISH), response.getBody().errors().get("name"));
    }

    @Test
    void updateTeam_GivenInvalidDTOValues_ReturnsBadRequest_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-2", HttpMethod.PUT, new HttpEntity<>(invalidTeamDTO, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(COUNTRY_NAME_SIZE, SPAIN_LOCALE), response.getBody().errors().get("countryDTO.name"));
        Assertions.assertEquals(getErrorMessage(COUNTRY_CODE_INVALID, SPAIN_LOCALE), response.getBody().errors().get("countryDTO.code"));
        Assertions.assertEquals(getErrorMessage(TEAM_FOUNDED_PAST, SPAIN_LOCALE), response.getBody().errors().get("founded"));
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, SPAIN_LOCALE), response.getBody().errors().get("level"));
        Assertions.assertEquals(getErrorMessage(TEAM_NAME_SIZE, SPAIN_LOCALE), response.getBody().errors().get("name"));
    }

    @Test
    void updateTeam_GivenInvalidDTONullValues_ReturnsBadRequest() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-2", HttpMethod.PUT, new HttpEntity<>(invalidTeamDTONullValues, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(COUNTRY_NAME_BLANK, Locale.ENGLISH), response.getBody().errors().get("countryDTO.name"));
        Assertions.assertEquals(getErrorMessage(COUNTRY_CODE_INVALID, Locale.ENGLISH), response.getBody().errors().get("countryDTO.code"));
        Assertions.assertEquals(getErrorMessage(TEAM_PICTURE_BLANK, Locale.ENGLISH), response.getBody().errors().get("picture"));
        Assertions.assertEquals(getErrorMessage(TEAM_FOUNDED_BLANK, Locale.ENGLISH), response.getBody().errors().get("founded"));
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH), response.getBody().errors().get("level"));
        Assertions.assertEquals(getErrorMessage(TEAM_NAME_BLANK, Locale.ENGLISH), response.getBody().errors().get("name"));
    }

    @Test
    void updateTeam_GivenInvalidDTONullValues_ReturnsBadRequest_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-2", HttpMethod.PUT, new HttpEntity<>(invalidTeamDTONullValues, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(COUNTRY_NAME_BLANK, SPAIN_LOCALE), response.getBody().errors().get("countryDTO.name"));
        Assertions.assertEquals(getErrorMessage(COUNTRY_CODE_INVALID, SPAIN_LOCALE), response.getBody().errors().get("countryDTO.code"));
        Assertions.assertEquals(getErrorMessage(TEAM_PICTURE_BLANK, SPAIN_LOCALE), response.getBody().errors().get("picture"));
        Assertions.assertEquals(getErrorMessage(TEAM_FOUNDED_BLANK, SPAIN_LOCALE), response.getBody().errors().get("founded"));
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, SPAIN_LOCALE), response.getBody().errors().get("level"));
        Assertions.assertEquals(getErrorMessage(TEAM_NAME_BLANK, SPAIN_LOCALE), response.getBody().errors().get("name"));
    }

    @Test
    void updateTeam_GivenInvalidDTONullCountryDTO_ReturnsBadRequest() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-2", HttpMethod.PUT, new HttpEntity<>(invalidTeamDTONullCountryDTO, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_COUNTRY_NULL, Locale.ENGLISH), response.getBody().errors().get("countryDTO"));
        Assertions.assertEquals(getErrorMessage(TEAM_PICTURE_BLANK, Locale.ENGLISH), response.getBody().errors().get("picture"));
        Assertions.assertEquals(getErrorMessage(TEAM_FOUNDED_BLANK, Locale.ENGLISH), response.getBody().errors().get("founded"));
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH), response.getBody().errors().get("level"));
        Assertions.assertEquals(getErrorMessage(TEAM_NAME_BLANK, Locale.ENGLISH), response.getBody().errors().get("name"));
    }

    @Test
    void updateTeam_GivenInvalidDTONullCountryDTO_ReturnsBadRequest_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-2", HttpMethod.PUT, new HttpEntity<>(invalidTeamDTONullCountryDTO, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_COUNTRY_NULL, SPAIN_LOCALE), response.getBody().errors().get("countryDTO"));
        Assertions.assertEquals(getErrorMessage(TEAM_PICTURE_BLANK, SPAIN_LOCALE), response.getBody().errors().get("picture"));
        Assertions.assertEquals(getErrorMessage(TEAM_FOUNDED_BLANK, SPAIN_LOCALE), response.getBody().errors().get("founded"));
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, SPAIN_LOCALE), response.getBody().errors().get("level"));
        Assertions.assertEquals(getErrorMessage(TEAM_NAME_BLANK, SPAIN_LOCALE), response.getBody().errors().get("name"));
    }

    @Test
    void updateTeam_GivenInvalidTeamId_ReturnsNotFound() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-1000", HttpMethod.PUT, new HttpEntity<>(bayernMunchen, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_NOT_FOUND.name(), new Object[]{-1000}, Locale.ENGLISH), response.getBody().errors().get("error"));
    }


    @Test
    void updateTeam_GivenInvalidTeamId_ReturnsNotFound_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-1000", HttpMethod.PUT, new HttpEntity<>(bayernMunchen, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_NOT_FOUND.name(), new Object[]{-1000}, SPAIN_LOCALE), response.getBody().errors().get("error"));
    }

    @Test
    void updateTeam_GivenExistentTeam_ReturnsBadRequest() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/19", HttpMethod.PUT, new HttpEntity<>(corinthians, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(ERROR_TO_PERSIST.name(), Locale.ENGLISH), response.getBody().errors().get("error"));
    }

    @Test
    void updateTeam_GivenExistentTeam_ReturnsBadRequest_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/19", HttpMethod.PUT, new HttpEntity<>(corinthians, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(ERROR_TO_PERSIST.name(), SPAIN_LOCALE), response.getBody().errors().get("error"));
    }


    @Test
    void updateTeam_GivenValidDTOAndNotAllowedRoleUser_ReturnsForbidden() {
        headers.setBearerAuth(getAccessTokenForNotAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<TeamDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-2", HttpMethod.PUT, new HttpEntity<>(bayernMunchen, headers), TeamDTO.class);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateTeam_GivenValidDTOAndInvalidAccessToken_ReturnsUnauthorized() {
        headers.setBearerAuth("XX");
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<TeamDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-2", HttpMethod.PUT, new HttpEntity<>(bayernMunchen, headers), TeamDTO.class);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    @Test
    void updateTeam_GivenValidDTOAndAllowedRoleUser_ReturnsOK() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<TeamDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/19", HttpMethod.PUT, new HttpEntity<>(bayernMunchen, headers), TeamDTO.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals("FC Bayern de Munchen", response.getBody().name());
        Assertions.assertEquals("DE", response.getBody().countryDTO().code());
    }

    @Test
    void updateTeamLevel_GivenLowerRangeLevelNotAllowed_ReturnsBadRequest() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-3/level/0.9", HttpMethod.PATCH, new HttpEntity<>(headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH), response.getBody().errors().get("updateTeamLevel.level"));
    }

    @Test
    void updateTeamLevel_GivenLowerRangeLevelNotAllowed_ReturnsBadRequest_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-3/level/0.9", HttpMethod.PATCH, new HttpEntity<>(headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, SPAIN_LOCALE), response.getBody().errors().get("updateTeamLevel.level"));
    }

    @Test
    void updateTeamLevel_GivenUpperRangeLevelNotAllowed_ReturnsBadRequest() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-3/level/10.1", HttpMethod.PATCH, new HttpEntity<>(headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH), response.getBody().errors().get("updateTeamLevel.level"));
    }

    @Test
    void updateTeamLevel_GivenUpperRangeLevelNotAllowed_ReturnsBadRequest_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-3/level/10.1", HttpMethod.PATCH, new HttpEntity<>(headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, SPAIN_LOCALE), response.getBody().errors().get("updateTeamLevel.level"));
    }

    @Test
    void updateTeamLevel_GivenInvalidTeamId_ReturnsNotFound() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-1000/level/8", HttpMethod.PATCH, new HttpEntity<>(headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_NOT_FOUND.name(), new Object[]{-1000}, Locale.ENGLISH), response.getBody().errors().get("error"));
    }

    @Test
    void updateTeamLevel_GivenInvalidTeamId_ReturnsNotFound_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-1000/level/8", HttpMethod.PATCH, new HttpEntity<>(headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_NOT_FOUND.name(), new Object[]{-1000}, SPAIN_LOCALE), response.getBody().errors().get("error"));
    }

    @Test
    void updateTeamLevel_GivenValidRangeLevelAndNotAllowedRoleUser_ReturnsForbidden() {
        headers.setBearerAuth(getAccessTokenForNotAllowedRoleUser());
        ResponseEntity<TeamDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-3/level/8.8", HttpMethod.PATCH, new HttpEntity<>(headers), TeamDTO.class);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateTeamLevel_GivenValidRangeLevelAndInvalidAccessToken_ReturnsUnauthorized() {
        headers.setBearerAuth("XX");
        ResponseEntity<TeamDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-3/level/8.8", HttpMethod.PATCH, new HttpEntity<>(headers), TeamDTO.class);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    @Test
    void updateTeamLevel_GivenValidRangeLevelAndAllowedRoleUser_ReturnsOK() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        ResponseEntity<TeamDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/20/level/8.8", HttpMethod.PATCH, new HttpEntity<>(headers), TeamDTO.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(8.350000047683716, response.getBody().level());
    }

    @Test
    void deleteTeam_GivenInvalidTeamId_ReturnsNotFound() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-1000", HttpMethod.DELETE, new HttpEntity<>(headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_NOT_FOUND.name(), new Object[]{-1000}, Locale.ENGLISH), response.getBody().errors().get("error"));
    }

    @Test
    void deleteTeam_GivenInvalidTeamId_ReturnsNotFound_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-1000", HttpMethod.DELETE, new HttpEntity<>(headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_NOT_FOUND.name(), new Object[]{-1000}, SPAIN_LOCALE), response.getBody().errors().get("error"));
    }

    @Test
    void deleteTeam_GivenValidTeamIdAndNotAllowedRoleUser_ReturnsForbidden() {
        headers.setBearerAuth(getAccessTokenForNotAllowedRoleUser());
        ResponseEntity<Void> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-4", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deleteTeam_GivenValidTeamIdAndInvalidAccessToken_ReturnsUnauthorized() {
        headers.setBearerAuth("XX");
        ResponseEntity<Void> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/-4", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void deleteTeam_GivenValidTeamIdAndAllowedRoleUser_ReturnsNoContent() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        ResponseEntity<Void> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH + "/18", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void createTeam_GivenInvalidDTOValues_ReturnsBadRequest() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH, HttpMethod.POST, new HttpEntity<>(invalidTeamDTO, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(COUNTRY_NAME_SIZE, Locale.ENGLISH), response.getBody().errors().get("countryDTO.name"));
        Assertions.assertEquals(getErrorMessage(COUNTRY_CODE_INVALID, Locale.ENGLISH), response.getBody().errors().get("countryDTO.code"));
        Assertions.assertEquals(getErrorMessage(TEAM_FOUNDED_PAST, Locale.ENGLISH), response.getBody().errors().get("founded"));
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH), response.getBody().errors().get("level"));
        Assertions.assertEquals(getErrorMessage(TEAM_NAME_SIZE, Locale.ENGLISH), response.getBody().errors().get("name"));
    }

    @Test
    void createTeam_GivenInvalidDTOValues_ReturnsBadRequest_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH, HttpMethod.POST, new HttpEntity<>(invalidTeamDTO, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(COUNTRY_NAME_SIZE, SPAIN_LOCALE), response.getBody().errors().get("countryDTO.name"));
        Assertions.assertEquals(getErrorMessage(COUNTRY_CODE_INVALID, SPAIN_LOCALE), response.getBody().errors().get("countryDTO.code"));
        Assertions.assertEquals(getErrorMessage(TEAM_FOUNDED_PAST, SPAIN_LOCALE), response.getBody().errors().get("founded"));
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, SPAIN_LOCALE), response.getBody().errors().get("level"));
        Assertions.assertEquals(getErrorMessage(TEAM_NAME_SIZE, SPAIN_LOCALE), response.getBody().errors().get("name"));

    }

    @Test
    void createTeam_GivenInvalidDTONullValues_ReturnsBadRequest() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH, HttpMethod.POST, new HttpEntity<>(invalidTeamDTONullValues, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(COUNTRY_NAME_BLANK, Locale.ENGLISH), response.getBody().errors().get("countryDTO.name"));
        Assertions.assertEquals(getErrorMessage(COUNTRY_CODE_INVALID, Locale.ENGLISH), response.getBody().errors().get("countryDTO.code"));
        Assertions.assertEquals(getErrorMessage(TEAM_PICTURE_BLANK, Locale.ENGLISH), response.getBody().errors().get("picture"));
        Assertions.assertEquals(getErrorMessage(TEAM_FOUNDED_BLANK, Locale.ENGLISH), response.getBody().errors().get("founded"));
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH), response.getBody().errors().get("level"));
        Assertions.assertEquals(getErrorMessage(TEAM_NAME_BLANK, Locale.ENGLISH), response.getBody().errors().get("name"));
    }


    @Test
    void createTeam_GivenInvalidDTONullValues_ReturnsBadRequest_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH, HttpMethod.POST, new HttpEntity<>(invalidTeamDTONullValues, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(COUNTRY_NAME_BLANK, SPAIN_LOCALE), response.getBody().errors().get("countryDTO.name"));
        Assertions.assertEquals(getErrorMessage(COUNTRY_CODE_INVALID, SPAIN_LOCALE), response.getBody().errors().get("countryDTO.code"));
        Assertions.assertEquals(getErrorMessage(TEAM_PICTURE_BLANK, SPAIN_LOCALE), response.getBody().errors().get("picture"));
        Assertions.assertEquals(getErrorMessage(TEAM_FOUNDED_BLANK, SPAIN_LOCALE), response.getBody().errors().get("founded"));
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, SPAIN_LOCALE), response.getBody().errors().get("level"));
        Assertions.assertEquals(getErrorMessage(TEAM_NAME_BLANK, SPAIN_LOCALE), response.getBody().errors().get("name"));
    }

    @Test
    void createTeam_GivenInvalidDTONullCountryDTO_ReturnsBadRequest() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH, HttpMethod.POST, new HttpEntity<>(invalidTeamDTONullCountryDTO, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_COUNTRY_NULL, Locale.ENGLISH), response.getBody().errors().get("countryDTO"));
        Assertions.assertEquals(getErrorMessage(TEAM_PICTURE_BLANK, Locale.ENGLISH), response.getBody().errors().get("picture"));
        Assertions.assertEquals(getErrorMessage(TEAM_FOUNDED_BLANK, Locale.ENGLISH), response.getBody().errors().get("founded"));
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, Locale.ENGLISH), response.getBody().errors().get("level"));
        Assertions.assertEquals(getErrorMessage(TEAM_NAME_BLANK, Locale.ENGLISH), response.getBody().errors().get("name"));
    }

    @Test
    void createTeam_GivenInvalidDTONullCountryDTO_ReturnsBadRequest_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH, HttpMethod.POST, new HttpEntity<>(invalidTeamDTONullCountryDTO, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(TEAM_COUNTRY_NULL, SPAIN_LOCALE), response.getBody().errors().get("countryDTO"));
        Assertions.assertEquals(getErrorMessage(TEAM_PICTURE_BLANK, SPAIN_LOCALE), response.getBody().errors().get("picture"));
        Assertions.assertEquals(getErrorMessage(TEAM_FOUNDED_BLANK, SPAIN_LOCALE), response.getBody().errors().get("founded"));
        Assertions.assertEquals(getErrorMessage(TEAM_LEVEL_INVALID, SPAIN_LOCALE), response.getBody().errors().get("level"));
        Assertions.assertEquals(getErrorMessage(TEAM_NAME_BLANK, SPAIN_LOCALE), response.getBody().errors().get("name"));
    }

    @Test
    void createTeam_GivenExistentTeam_ReturnsBadRequest() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH, HttpMethod.POST, new HttpEntity<>(corinthians, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(ERROR_TO_PERSIST.name(), Locale.ENGLISH), response.getBody().errors().get("error"));
    }

    @Test
    void createTeam_GivenExistentTeam_ReturnsBadRequest_Spanish() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        headers.add(ACCEPT_LANGUAGE, ES);
        ResponseEntity<ErrorDTO> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH, HttpMethod.POST, new HttpEntity<>(corinthians, headers), ErrorDTO.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(APPLICATION_JSON, response.getHeaders().getContentType());
        Assertions.assertEquals(getErrorMessage(ERROR_TO_PERSIST.name(), SPAIN_LOCALE), response.getBody().errors().get("error"));
    }

    @Test
    void createTeam_GivenValidDTOAndNotAllowedRoleUser_ReturnsForbidden() {
        headers.setBearerAuth(getAccessTokenForNotAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<Void> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH, HttpMethod.POST, new HttpEntity<>(bayerLeverkusen, headers), Void.class);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createTeam_GivenValidDTOAndInvalidAccessToken_ReturnsUnauthorized() {
        headers.setBearerAuth("XX");
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<Void> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH, HttpMethod.POST, new HttpEntity<>(bayerLeverkusen, headers), Void.class);

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createTeam_GivenValidDTOAndAllowedRoleUser_ReturnsCreated() {
        headers.setBearerAuth(getAccessTokenForAllowedRoleUser());
        headers.setContentType(APPLICATION_JSON);
        ResponseEntity<Void> response = testRestTemplate.exchange(TEAM_RESOURCE_PATH, HttpMethod.POST, new HttpEntity<>(bayerLeverkusen, headers), Void.class);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertTrue(response.getHeaders().getLocation().toString().contains("/v1/teams/3"));
    }

    private String getErrorMessage(String messageKeyWithBraces, Locale locale) {
        return getErrorMessage(messageKeyWithBraces, null, locale);
    }

    private String getErrorMessage(String messageKeyWithBraces, Object[] args, Locale locale) {
        return messageSource.getMessage(messageKeyWithBraces.replaceAll("[{}]", ""), args, locale);
    }

    private void createCorinthiansTeam() {
        CountryDTO brazil = new CountryDTO("Brazil", "BR");
        corinthians = TeamDTO.builder()
                .countryDTO(brazil)
                .founded(LocalDate.of(1910, 9, 1))
                .level(8.0d)
                .name("Sport Club Corinthians Paulista")
                .picture("https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/corinthians.jpg")
                .nickName("Timao")
                .build();
    }


    private void createBayerMunchenTeam() {
        CountryDTO germany = new CountryDTO("Germany", "DE");
        bayernMunchen = TeamDTO.builder()
                .countryDTO(germany)
                .founded(LocalDate.of(1900, 2, 27))
                .level(9.1d)
                .name("FC Bayern de Munchen")
                .picture("https://storage.googleapis.com/www-paredro-com/uploads/2019/02/%E2%96%B7-Esta-es-la-historia-del-logo-del-Bayern-Mu%CC%81nich-el-gigante-de-Baviera.jpg")
                .nickName("Bayer")
                .build();
    }


    private void createBayerLeverkusen() {
        CountryDTO germany = new CountryDTO("Germany", "DE");
        bayerLeverkusen = TeamDTO.builder()
                .countryDTO(germany)
                .founded(LocalDate.of(1904, 6, 1))
                .level(8.1d)
                .name("Bayer 04 Leverkusen Fußball GmbH")
                .picture("https://upload.wikimedia.org/wikipedia/en/thumb/5/59/Bayer_04_Leverkusen_logo.svg/345px-Bayer_04_Leverkusen_logo.svg.png")
                .build();
    }

    private void createInvalidTeamDTO() {
        CountryDTO invalidCountryDTO = new CountryDTO("XX", "XX");
        invalidTeamDTO = TeamDTO.builder()
                .countryDTO(invalidCountryDTO)
                .founded(LocalDate.now())
                .level(11d)
                .name("ss")
                .picture("picture")
                .nickName("nickname")
                .build();
    }

    private void createInvalidTeamDTONullValues() {
        CountryDTO invalidCountryDTONullValues = new CountryDTO(null, null);
        invalidTeamDTONullValues = TeamDTO.builder()
                .countryDTO(invalidCountryDTONullValues)
                .build();
    }

    private void createInvalidTeamDTONullCountryDTO() {
        invalidTeamDTONullCountryDTO = TeamDTO.builder().build();
    }

    private String getAccessTokenForAllowedRoleUser() {
        return getAccessToken("teamuser", "teamuser");
    }

    private String getAccessTokenForNotAllowedRoleUser() {
        return getAccessToken("test", "test");
    }

    private String getAccessToken(String username, String password) {
        HttpEntity<MultiValueMap<String, String>> tokenRequest = createTokenRequest(username, password);
        ResponseEntity<AccessToken> response = testRestTemplate.exchange(authServerUrl.concat("/protocol/openid-connect/token"), HttpMethod.POST, tokenRequest, AccessToken.class);

        return response.getBody().accessToken();
    }

    private HttpEntity<MultiValueMap<String, String>> createTokenRequest(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", "6fe5572d-d0f7-4121-8fc4-d2768bf82836");
        body.add("username", username);
        body.add("password", password);

        return new HttpEntity<>(body, headers);
    }

}
