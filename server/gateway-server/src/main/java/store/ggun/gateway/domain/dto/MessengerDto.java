package store.ggun.gateway.domain.dto;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessengerDto {
    private String message;
    private Object data;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpired;
    private Long refreshTokenExpired;
}
