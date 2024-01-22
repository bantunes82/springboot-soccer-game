package springboot.soccer.game.team.domainobject;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@MappedSuperclass
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public sealed class BaseDO permits CountryDO, TeamDO {

    @Version
    private Long version;

}
