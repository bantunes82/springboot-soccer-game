package springboot.soccer.game.team.dataaccessobject;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import springboot.soccer.game.team.domainobject.TeamDO;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

public interface TeamRepository extends PagingAndSortingRepository<TeamDO, Long> {

    default Optional<TeamDO> findRandomAndDeletedIsFalse() {
        long countTeams = count();
        int randomTeam = count() == 0 ? 1 : new SecureRandom().nextInt((int) countTeams);

        Pageable pageable = PageRequest.of(randomTeam,1);

        return  findByDeletedIsFalse(pageable)
                .stream()
                .findFirst();
    }

    List<TeamDO> findByDeletedIsFalse(Pageable pageable);

    Optional<TeamDO> findByIdAndDeletedIsFalse(Long id);

    List<TeamDO> findByNameAndDeletedIsFalse(String name);

    List<TeamDO> findByCountryDOCodeAndDeletedIsFalse(String countryCode, Pageable pageable);


}
