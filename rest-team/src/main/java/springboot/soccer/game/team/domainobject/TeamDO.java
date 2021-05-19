package springboot.soccer.game.team.domainobject;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import springboot.soccer.game.team.validation.ConstraintMessage;
import springboot.soccer.game.team.validation.Range;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "team", uniqueConstraints = @UniqueConstraint(name = "uc_team", columnNames = {"name", "country"}))
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class TeamDO {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = ConstraintMessage.TEAM_NAME_BLANK)
    @Size(min = 3, max = 50, message = ConstraintMessage.TEAM_NAME_SIZE)
    private String name;

    @Column(name = "nick_name")
    private String nickName;

    @Past(message = ConstraintMessage.TEAM_FOUNDED_PAST)
    @NotNull(message = ConstraintMessage.TEAM_FOUNDED_BLANK)
    private LocalDate founded;

    @Range(min = 1.0, max = 10.0)
    private Double level;

    @NotBlank(message = ConstraintMessage.TEAM_PICTURE_BLANK)
    private String picture;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @NotNull(message = ConstraintMessage.TEAM_COUNTRY_NULL)
    @JoinColumn(name = "country", nullable = false)
    @Valid
    private CountryDO countryDO;

    public Optional<String> getNickName() {
        return Optional.ofNullable(nickName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamDO teamDO = (TeamDO) o;
        return name.equals(teamDO.name) &&
                countryDO.equals(teamDO.countryDO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, countryDO);
    }
}