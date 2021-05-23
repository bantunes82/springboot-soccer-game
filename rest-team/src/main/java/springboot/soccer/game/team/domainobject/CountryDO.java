package springboot.soccer.game.team.domainobject;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import springboot.soccer.game.team.validation.ConstraintMessage;
import springboot.soccer.game.team.validation.CountryCode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(name = "country", uniqueConstraints = {
        @UniqueConstraint(name = "uc_country_code", columnNames = {"code"})
})
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class CountryDO extends BaseDO {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = ConstraintMessage.COUNTRY_NAME_BLANK)
    @Size(min = 3, max = 20, message = ConstraintMessage.COUNTRY_NAME_SIZE)
    private String name;

    @CountryCode
    private String code;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountryDO countryDO = (CountryDO) o;
        return code.equals(countryDO.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}