package store.ggun.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.ggun.user.domain.vo.Registration;
import store.ggun.user.domain.vo.Role;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDomain {
    private String id;
    private String email;
    private String name;
    private String profile;
    private List<Role> roles;
    private Registration registration;
}