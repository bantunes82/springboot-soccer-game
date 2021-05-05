package springboot.soccer.game.team.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakConfiguration {

    public static String kEYCLOAK_REALM;

    public static String kEYCLOAK_RESOURCE;

    public static boolean kEYCLOAK_BEARER_ONLY;

    public static String kEYCLOAK_AUTH_ROLES;

    public static String kEYCLOAK_PATTERNS;

    public static String kEYCLOAK_METHOD0;

    public static String kEYCLOAK_METHOD1;

    public static String kEYCLOAK_METHOD2;

    public static String kEYCLOAK_METHOD3;

    @Value("${keycloak.realm}")
    public void setKeycloakRealm(String keycloakRealm) {
        this.kEYCLOAK_REALM = keycloakRealm;
    }

    @Value("${keycloak.resource}")
    public void setKeycloakResource(String keycloakResource) {
        this.kEYCLOAK_RESOURCE = keycloakResource;
    }

    @Value("${keycloak.bearer-only}")
    public void setKeycloakBearerOnly(boolean keycloakBearerOnly) {
        this.kEYCLOAK_BEARER_ONLY = keycloakBearerOnly;
    }

    @Value("${keycloak.security-constraints[0].authRoles[0]}")
    public void setKeycloakAuthRoles(String keycloakAuthRoles) {
        this.kEYCLOAK_AUTH_ROLES = keycloakAuthRoles;
    }

    @Value("${keycloak.security-constraints[0].securityCollections[0].patterns[0]}")
    public void setKeycloakPatterns(String keycloakPatterns) {
        this.kEYCLOAK_PATTERNS = keycloakPatterns;
    }

    @Value("${keycloak.security-constraints[0].securityCollections[0].methods[0]}")
    public void setKeycloakMethod0(String keycloakMethod0) {
        this.kEYCLOAK_METHOD0 = keycloakMethod0;
    }

    @Value("${keycloak.security-constraints[0].securityCollections[0].methods[1]}")
    public void setKeycloakMethod1(String keycloakMethod1) {
        this.kEYCLOAK_METHOD1 = keycloakMethod1;
    }

    @Value("${keycloak.security-constraints[0].securityCollections[0].methods[2]}")
    public void setKeycloakMethod2(String keycloakMethod2) {
        this.kEYCLOAK_METHOD2 = keycloakMethod2;
    }

    @Value("${keycloak.security-constraints[0].securityCollections[0].methods[3]}")
    public void setKeycloakMethod3(String keycloakMethod3) {
        this.kEYCLOAK_METHOD3 = keycloakMethod3;
    }
}
