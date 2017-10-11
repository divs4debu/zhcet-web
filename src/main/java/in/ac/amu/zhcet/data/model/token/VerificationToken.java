package in.ac.amu.zhcet.data.model.token;

import in.ac.amu.zhcet.data.model.base.BaseEntity;
import in.ac.amu.zhcet.data.model.user.UserAuth;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VerificationToken extends BaseEntity {

    private static final int EXPIRATION = 60 * 3;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String email;
    @NotNull
    private String token;
    private boolean used;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private UserAuth user;

    @NotNull
    private Date expiry;

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    public VerificationToken(UserAuth userAuth, String token, String email) {
        this.user = userAuth;
        this.token = token;
        this.email = email;
    }

    @PrePersist
    public void prePersist(){
        if (expiry != null) return;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 3);
        expiry = calendar.getTime();
    }

}