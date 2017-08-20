package in.ac.amu.zhcet.data.service.user;

import in.ac.amu.zhcet.data.model.Department;
import in.ac.amu.zhcet.data.model.base.user.Type;
import in.ac.amu.zhcet.data.model.base.user.UserAuth;
import in.ac.amu.zhcet.data.service.UserDetailService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
@Setter
public class CustomUser extends User {

    private String name;
    private String avatar;
    private Type type;
    private Department department;

    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public CustomUser name(String name) {
        setName(name);
        return this;
    }

    public CustomUser avatar(String avatar) {
        setAvatar(avatar);
        return this;
    }

    public CustomUser type(Type type) {
        setType(type);
        return this;
    }

    public CustomUser department(Department department) {
        setDepartment(department);
        return this;
    }

    public static CustomUser fromUserAuth(UserAuth user) {
        return new CustomUser(user.getUserId(), user.getPassword(), UserDetailService.getAuthorities(user.getRoles()))
                .name(user.getName())
                .avatar(user.getDetails().getAvatarUrl())
                .type(user.getType())
                .department(user.getDetails().getDepartment());
    }

}
