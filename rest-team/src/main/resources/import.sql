-- CREATE Script for init of DB


--CREATE COUNTRIES
INSERT INTO country(id, name, code) VALUES (-1, 'Brazil', 'BR');
INSERT INTO country(id, name, code) VALUES (-2, 'Spain', 'ES');
INSERT INTO country(id, name, code) VALUES (-3, 'Germany', 'DE');
INSERT INTO country(id, name, code) VALUES (-4, 'Argentina', 'AR');
INSERT INTO country(id, name, code) VALUES (-5, 'England', 'GB');
INSERT INTO country(id, name, code) VALUES (-6, 'Italy', 'IT');


-- CREATE SOCCER TEAM FROM BRAZIL
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-1, 'Sport Club Corinthians Paulista', 'Timao', DATE('1910-09-01'), 8.6, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/corinthians.jpg', 'false', -1);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-2, 'Sociedade Esportiva Palmeiras', 'Porco', DATE('1914-08-26'), 7.4, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/palmeiras.jpg', 'false', -1);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-3, 'São Paulo Futebol Clube', 'Tricolor Paulista', DATE('1930-01-25'), 6.9, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/saopaulo.jpg', 'false', -1);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-4, 'Santos Fútbol Club', 'Peixe', DATE('1912-04-14'), 7.1, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/santos.jpg', 'false', -1);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-5, 'Clube de Regatas do Flamengo', 'Mengo', DATE('1895-11-15'), 8.5, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/flamengo.jpg', 'false', -1);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-6, 'Club de Regatas Vasco da Gama', 'Vascao', DATE('1898-08-21'), 6.3, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/vasco.jpg', 'false', -1);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-7, 'Botafogo de Futebol e Regatas', 'Fogao', DATE('1904-08-12'), 6.5, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/botafogo.jpg', 'false', -1);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-8, 'Fluminense Football Club', 'Pó de arroz', DATE('1902-07-21'), 6.4, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/fluminense.jpg', 'false', -1);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-9, 'Clube Atlético Mineiro', 'Galo', DATE('1908-03-25'), 8.1, 'https://e.imguol.com/futebol/brasoes/40x40/atletico-mg.png', 'false', -1);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-10, 'Cruzeiro Esporte Clube', 'Zero', DATE('1921-01-02'), 5.1, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/cruzeiro.jpg', 'false', -1);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-11, 'Grêmio Foot-Ball Porto Alegrense', 'Tricolor Gaucho', DATE('1903-09-15'), 7.9, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/gremio.jpg', 'false', -1);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-12, 'Sport Club Internacional', 'Inter', DATE('1909-04-04'), 7.7, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/internacional.jpg', 'false', -1);


-- CREATE SOCCER TEAM FROM SPAIN
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-13, 'Fútbol Club Barcelona', 'Barca', DATE('1899-11-29'), 9.1, 'https://ssl.gstatic.com/onebox/media/sports/logos/paYnEE8hcrP96neHRNofhQ_96x96.png', 'false', -2);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-14, 'Real Madrid Club de Fútbol', 'Meregnues', DATE('1902-03-06'), 9.0, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/real-madrid.png', 'false', -2);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-15, 'Club Atlético de Madrid', 'Cochoneros', DATE('1903-04-26'), 8.6, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/AtleticoMadrid.png', 'false', -2);


-- CREATE SOCCER TEAM FROM GERMANY
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-16, 'Bayern de Múnich', 'Bavaros', DATE('1900-02-27'), 9.4, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/Bayern-Munchen.png', 'false', -3);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-17, 'Borussia Dortmund', null, DATE('1909-12-19'), 8.8, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/borussia.png', 'false', -3);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-18, 'Eintracht Frankfurt', 'Frankfurt', DATE('1899-03-08'), 7.2, 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Eintracht_Frankfurt_Logo.svg/550px-Eintracht_Frankfurt_Logo.svg.png', 'false', -3);


-- CREATE SOCCER TEAM FROM ARGENTINA
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-19, 'Club Atlético Boca Juniors', 'Boca', DATE('1905-04-03'), 8.1, 'https://e00-co-marca.uecdn.es/claro/assets/multimedia/imagenes/2020/08/31/15989012119137.jpg', 'false', -4);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-20, 'Club Atlético River Plate', 'River', DATE('1901-05-25'), 7.9, 'https://upload.wikimedia.org/wikipedia/commons/f/fc/Escudo_del_Club_Atl%C3%A9tico_River_Plate.png', 'false', -4);


-- CREATE SOCCER TEAM FROM ENGLAND
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-21, 'Manchester United Football Club', 'Red Devils', DATE('1878-03-05'), 8.3, 'https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcQfnjR__WVRQ2L4mimWFIDQDCCDTqHUDyEoIg&usqp=CAU', 'false', -5);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-22, 'Liverpool Football Club', 'The Reds', DATE('1892-06-03'), 9.3, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/liverpool.png', 'false', -5);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-23, 'Manchester City Football Club', 'Citizens', DATE('1894-04-16'), 9.2, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/Manchester_City.png', 'false', -5);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-24, 'Chelsea Football Club', 'The Blues', DATE('1905-03-10'), 8.8, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/chelsea.png', 'false', -5);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-25, 'Tottenham Hotspur Football Club', 'Spurs', DATE('1882-09-05'), 8.8, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/Tottenham_Hotspur.png', 'false', -5);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-26, 'Arsenal Football Club', 'The Gunners', DATE('1886-12-01'), 8.4, 'https://images-na.ssl-images-amazon.com/images/I/81tHDkqURwL._AC_SL1500_.jpg', 'false', -5);


-- CREATE SOCCER TEAM FROM ITALY
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-27, 'Juventus de Turín', 'La Vecchia Signora', DATE('1897-11-01'), 9.0, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/juventus.png', 'false', -6);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-28, 'Football Club Internazionale Milano', 'Nerazzurri', DATE('1908-03-09'), 8.9, 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/89/FC_Internazionale_Milano_2014.svg/160px-FC_Internazionale_Milano_2014.svg.png', 'false', -6);
INSERT INTO team(id, name, nick_name, founded, level, picture, deleted, country) VALUES (-29, 'Associazione Calcio Milan', null, DATE('1899-06-16'), 8.6, 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/Logo_of_AC_Milan.svg/130px-Logo_of_AC_Milan.svg.png', 'false', -6);