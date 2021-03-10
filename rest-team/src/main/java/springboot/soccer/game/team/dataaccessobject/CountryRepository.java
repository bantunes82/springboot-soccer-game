package springboot.soccer.game.team.dataaccessobject;

import org.springframework.data.repository.CrudRepository;
import springboot.soccer.game.team.domainobject.CountryDO;

import java.util.Optional;

public interface CountryRepository extends CrudRepository<CountryDO,Long> {

    Optional<CountryDO> findByCode (String countryCode);
}
