package store.ggun.gateway.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.ggun.gateway.domain.vo.Registration;
import store.ggun.gateway.domain.vo.Role;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDomain {
    private String id;
    private String email;
    private String name;
    private List<Role> roles;
    private Registration registration;
}
