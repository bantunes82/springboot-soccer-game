package springboot.soccer.game.team.domainobject;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import springboot.soccer.game.team.validation.ConstraintMessage;
import springboot.soccer.game.team.validation.CountryCode;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(name = "country", uniqueConstraints = {
        @UniqueConstraint(name = "uc_country_code", columnNames = {"code"})
})
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CountryDO extends BaseDO {

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
        return o instanceof CountryDO countryDO &&
                code.equals(countryDO.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}