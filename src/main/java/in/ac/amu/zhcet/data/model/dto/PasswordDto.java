package in.ac.amu.zhcet.data.model.dto;

import lombok.Data;

@Data
public class PasswordDto {
    private String newPassword;
    private String confirmPassword;
}
