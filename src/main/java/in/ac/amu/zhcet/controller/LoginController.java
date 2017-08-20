package in.ac.amu.zhcet.controller;

import in.ac.amu.zhcet.data.model.PasswordResetToken;
import in.ac.amu.zhcet.data.model.dto.PasswordDto;
import in.ac.amu.zhcet.data.service.EmailService;
import in.ac.amu.zhcet.data.service.PasswordResetService;
import in.ac.amu.zhcet.data.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
public class LoginController {

    private final PasswordResetService passwordResetService;
    private final EmailService emailService;
    private final UserService userService;

    public LoginController(PasswordResetService passwordResetService, EmailService emailService, UserService userService) {
        this.passwordResetService = passwordResetService;
        this.emailService = emailService;
        this.userService = userService;
    }

    @GetMapping("/login/forgot_password")
    public String getForgetPassword() {
        return "forgot_password";
    }

    @PostMapping("/login/forgot_password")
    public String sendEmailLink(RedirectAttributes redirectAttributes, @RequestParam String email, HttpServletRequest request) {
        try {
            PasswordResetToken token = passwordResetService.generate(email);
            emailService.constructResetTokenEmail(getAppUrl(request), token.getToken(), token.getUserAuth());
            redirectAttributes.addFlashAttribute("reset_link_sent", true);
        }catch(UsernameNotFoundException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login/forgot_password";
        }

        return "redirect:/login";
    }

    @GetMapping("/login/reset_password")
    public String resetPassword(Model model, @RequestParam("id") String id, @RequestParam("token") String token){
        String result = passwordResetService.validate(id, token);
        if (result != null) {
            model.addAttribute("error", result);
            return "reset_password";
        }
        PasswordDto passwordDto = new PasswordDto();
        model.addAttribute("password", passwordDto);
        return "reset_password";
    }

    @PostMapping("/login/reset_password")
    public String savePassword(@Valid PasswordDto passwordDto, RedirectAttributes redirectAttributes){
        if(!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())){
            redirectAttributes.addFlashAttribute("save_errors", "Password Don't match");
            return "redirect:/login/reset_password";
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.changeUserPassword(user, passwordDto.getNewPassword());
        redirectAttributes.addFlashAttribute("reset_success", true);
        return "redirect:/login";
    }
    private String getAppUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

}
