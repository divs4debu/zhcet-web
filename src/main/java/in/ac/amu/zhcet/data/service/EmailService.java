package in.ac.amu.zhcet.data.service;

import groovy.util.logging.Slf4j;
import in.ac.amu.zhcet.data.model.base.user.UserAuth;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class EmailService {
    public SimpleMailMessage constructResetTokenEmail(String contextPath, String token, UserAuth userAuth) {
        String url = contextPath + "/login/reset_password?id=" + userAuth.getUserId() + "&token=" + token;
        System.out.println(url);
        String message = "You requested password reset on zhcet for userID: " + userAuth.getUserId() + "\r\n Please click this link to reset password";
        return constructEmail("Reset Password", message + " \r\n" + url, userAuth);
    }

    private SimpleMailMessage constructEmail(String subject, String body, UserAuth userAuth) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(userAuth.getEmail());
        email.setFrom(System.getenv("ZHCET_EMAIL"));
        return email;
    }
}
