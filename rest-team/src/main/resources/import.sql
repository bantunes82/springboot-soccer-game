-- CREATE Script for init of DB


--CREATE COUNTRIES
insert into country (id, name, code) values (-1, 'Brazil', 'BR');
insert into country (id, name, code) values (-2, 'Spain', 'ES');
insert into country (id, name, code) values (-3, 'Germany', 'DE');
insert into country (id, name, code) values (-4, 'Argentina', 'AR');
insert into country (id, name, code) values (-5, 'England', 'GB');
insert into country (id, name, code) values (-6, 'Italy', 'IT');


-- CREATE SOCCER TEAM FROM BRAZIL
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-1, 'Sport Club Corinthians Paulista', 'Timao', date('1910-09-01'), 8.6, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/corinthians.jpg', 'false', -1, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-2, 'Sociedade Esportiva Palmeiras', 'Porco', date('1914-08-26'), 7.4, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/palmeiras.jpg', 'false', -1, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-3, 'São Paulo Futebol Clube', 'Tricolor Paulista', date('1930-01-25'), 6.9, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/saopaulo.jpg', 'false', -1, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-4, 'Santos Fútbol Club', 'Peixe', date('1912-04-14'), 7.1, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/santos.jpg', 'false', -1, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-5, 'Clube de Regatas do Flamengo', 'Mengo', date('1895-11-15'), 8.5, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/flamengo.jpg', 'false', -1, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-6, 'Club de Regatas Vasco da Gama', 'Vascao', date('1898-08-21'), 6.3, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/vasco.jpg', 'false', -1, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-7, 'Botafogo de Futebol e Regatas', 'Fogao', date('1904-08-12'), 6.5, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/botafogo.jpg', 'false', -1, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-8, 'Fluminense Football Club', 'Pó de arroz', date('1902-07-21'), 6.4, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/fluminense.jpg', 'false', -1, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-9, 'Clube Atlético Mineiro', 'Galo', date('1908-03-25'), 8.1, 'https://e.imguol.com/futebol/brasoes/40x40/atletico-mg.png', 'false', -1, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-10, 'Cruzeiro Esporte Clube', 'Zero', date('1921-01-02'), 5.1, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/cruzeiro.jpg', 'false', -1, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-11, 'Grêmio Foot-Ball Porto Alegrense', 'Tricolor Gaucho', date('1903-09-15'), 7.9, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/gremio.jpg', 'false', -1, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-12, 'Sport Club Internacional', 'Inter', date('1909-04-04'), 7.7, 'https://conteudo.imguol.com.br//c/esporte/futebol/times/desktop/internacional.jpg', 'false', -1, 0);


-- CREATE SOCCER TEAM FROM SPAIN
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-13, 'Fútbol Club Barcelona', 'Barca', date('1899-11-29'), 9.1, 'https://ssl.gstatic.com/onebox/media/sports/logos/paYnEE8hcrP96neHRNofhQ_96x96.png', 'false', -2, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-14, 'Real Madrid Club de Fútbol', 'Meregnues', date('1902-03-06'), 9.0, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/real-madrid.png', 'false', -2, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-15, 'Club Atlético de Madrid', 'Cochoneros', date('1903-04-26'), 8.6, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/AtleticoMadrid.png', 'false', -2, 0);


-- CREATE SOCCER TEAM FROM GERMANY
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-16, 'Bayern de Múnich', 'Bavaros', date('1900-02-27'), 9.4, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/Bayern-Munchen.png', 'false', -3, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-17, 'Borussia Dortmund', null, date('1909-12-19'), 8.8, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/borussia.png', 'false', -3, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-18, 'Eintracht Frankfurt', 'Frankfurt', date('1899-03-08'), 7.2, 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/Eintracht_Frankfurt_Logo.svg/550px-Eintracht_Frankfurt_Logo.svg.png', 'false', -3, 0);


-- CREATE SOCCER TEAM FROM ARGENTINA
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-19, 'Club Atlético Boca Juniors', 'Boca', date('1905-04-03'), 8.1, 'https://e00-co-marca.uecdn.es/claro/assets/multimedia/imagenes/2020/08/31/15989012119137.jpg', 'false', -4, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-20, 'Club Atlético River Plate', 'River', date('1901-05-25'), 7.9, 'https://upload.wikimedia.org/wikipedia/commons/f/fc/Escudo_del_Club_Atl%C3%A9tico_River_Plate.png', 'false', -4, 0);


-- CREATE SOCCER TEAM FROM ENGLAND
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-21, 'Manchester United Football Club', 'Red Devils', date('1878-03-05'), 8.3, 'https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcQfnjR__WVRQ2L4mimWFIDQDCCDTqHUDyEoIg&usqp=CAU', 'false', -5, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-22, 'Liverpool Football Club', 'The Reds', date('1892-06-03'), 9.3, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/liverpool.png', 'false', -5, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-23, 'Manchester City Football Club', 'Citizens', date('1894-04-16'), 9.2, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/Manchester_City.png', 'false', -5, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-24, 'Chelsea Football Club', 'The Blues', date('1905-03-10'), 8.8, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/chelsea.png', 'false', -5, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-25, 'Tottenham Hotspur Football Club', 'Spurs', date('1882-09-05'), 8.8, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/Tottenham_Hotspur.png', 'false', -5, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-26, 'Arsenal Football Club', 'The Gunners', date('1886-12-01'), 8.4, 'https://images-na.ssl-images-amazon.com/images/I/81tHDkqURwL._AC_SL1500_.jpg', 'false', -5, 0);


-- CREATE SOCCER TEAM FROM ITALY
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-27, 'Juventus de Turín', 'La Vecchia Signora', date('1897-11-01'), 9.0, 'https://conteudo.imguol.com.br/p/pp/2020/eiplus/champions/times/juventus.png', 'false', -6, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-28, 'Football Club Internazionale Milano', 'Nerazzurri', date('1908-03-09'), 8.9, 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/89/FC_Internazionale_Milano_2014.svg/160px-FC_Internazionale_Milano_2014.svg.png', 'false', -6, 0);
insert into team (id, name, nick_name, founded, level, picture, deleted, country, version) values (-29, 'Associazione Calcio Milan', null, date('1899-06-16'), 8.6, 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/Logo_of_AC_Milan.svg/130px-Logo_of_AC_Milan.svg.png', 'false', -6, 0);