curl -i -X POST http://127.0.0.1:8080/auth/realms/team-realm/protocol/openid-connect/token \
--user team-client:6fe5572d-d0f7-4121-8fc4-d2768bf82836 \
 -H 'content-type: application/x-www-form-urlencoded' -d 'username=teamuser&password=teamuser&grant_type=password'