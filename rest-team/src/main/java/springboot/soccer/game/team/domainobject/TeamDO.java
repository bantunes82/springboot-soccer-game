package springboot.soccer.game.team.domainobject;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import springboot.soccer.game.team.validation.ConstraintMessage;
import springboot.soccer.game.team.validation.Range;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "team", uniqueConstraints = @UniqueConstraint(name = "uc_team", columnNames = {"name", "country"}))
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamDO extends BaseDO {

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
        return o instanceof TeamDO teamDO &&
                name.equals(teamDO.name) &&
                countryDO.equals(teamDO.countryDO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, countryDO);
    }
}