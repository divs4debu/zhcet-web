package in.ac.amu.zhcet.data.repository;


import in.ac.amu.zhcet.data.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>{
    PasswordResetToken findByUserAuth_UserId(String username);
    PasswordResetToken findByToken(String token);
}
