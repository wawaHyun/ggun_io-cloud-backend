package store.ggun.user.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LoginDTO {
    private String username ; //  email 로 username 대체 가능
    private String password ;
}
