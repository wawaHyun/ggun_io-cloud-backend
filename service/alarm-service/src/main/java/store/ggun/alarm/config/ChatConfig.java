package store.ggun.alarm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;
import store.ggun.alarm.domain.dto.ChatDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ChatConfig {

    @Bean
    public Map<String, Sinks.Many<ServerSentEvent<ChatDto>>> chatSinks() {
        return new ConcurrentHashMap<>();
    }
}
