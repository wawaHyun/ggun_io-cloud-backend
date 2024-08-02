package store.ggun.alarm.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import store.ggun.alarm.domain.dto.ChatDto;
import store.ggun.alarm.domain.dto.RoomDto;
import store.ggun.alarm.exception.ChatException;
import store.ggun.alarm.domain.model.ChatModel;
import store.ggun.alarm.domain.model.RoomModel;
import store.ggun.alarm.repository.ChatRepository;
import store.ggun.alarm.repository.RoomRepository;
import store.ggun.alarm.service.RoomService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final ChatRepository chatRepository;
    private final Map<String, Sinks.Many<ServerSentEvent<ChatDto>>> chatSinks;

    @PreDestroy
    public void close() {
        chatSinks.values().forEach(Sinks.Many::tryEmitComplete);
    }

    @Override
    public Mono<RoomModel> save(RoomDto dto) {
        return roomRepository.save(RoomModel.builder()
                .title(dto.getTitle())
                .members(dto == null ? new ArrayList<>() : dto.getMembers())
                .build());
    }

    @Override
    public Mono<ChatDto> saveChat(ChatDto chatDTO) {
        if (chatDTO == null || chatDTO.getRoomId() == null || chatDTO.getSenderId() == null) {
            log.error("ChatDTO, roomId, and senderId must not be null: {}", chatDTO); // 로그 추가
            return Mono.error(new IllegalArgumentException("ChatDTO, roomId, and senderId must not be null"));
        }

        log.info("saveChat called with roomId: {}, senderId: {}", chatDTO.getRoomId(), chatDTO.getSenderId());

//        boolean isMember = roomRepository.findById(chatDTO.getRoomId())
//                .map(i -> i.getMembers().contains(chatDTO.getSenderId()))
//                .blockOptional()
//                .orElse(false);

        return roomRepository.findById(chatDTO.getRoomId())
//                .filter(i -> i.getMembers().contains(chatDTO.getSenderId()))
                    .switchIfEmpty(Mono.error(new ChatException("Room not found or sender not a member")))
                    .flatMap(i -> chatRepository.save(ChatModel.builder()
                            .roomId(chatDTO.getRoomId())
                            .message(chatDTO.getMessage())
                            .senderId(chatDTO.getSenderId())
                            .senderName(chatDTO.getSenderName())
                            .createdAt(LocalDateTime.now())
                            .build()))
                    .flatMap(i -> Mono.just(ChatDto.builder()
                            .roomId(i.getRoomId())
                            .senderId(i.getSenderId())
                            .senderName(i.getSenderName())
                            .message(i.getMessage())
                            .createdAt(i.getCreatedAt())
                            .build()))
                    .doOnSuccess(i -> {
                        if (chatSinks.containsKey(i.getRoomId())) {
                            chatSinks.get(i.getRoomId()).tryEmitNext(ServerSentEvent.builder(i).build());
                        } else {
                            log.warn("No sink found for roomId: {}", i.getRoomId());
                        }
                    });

    }

    @Override
    public Mono<RoomModel> update(RoomDto dto) {
        return roomRepository.existsById(dto.getId())
                .flatMap(exists -> roomRepository.save(RoomModel.builder()
                        .id(dto.getId())
                        .title(dto.getTitle())
                        .members(dto.getMembers())
                        .build()));
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return roomRepository.existsById(id)
                .filter(exists -> exists)
                .flatMap(exists -> roomRepository.deleteById(id).thenReturn(exists));
    }

    @Override
    public Mono<RoomModel> findById(String id) {
        return roomRepository.findById(id);
    }

    @Override
    public Mono<ChatModel> findChatById(String id) {
        return chatRepository.findById(id);
    }

    @Override
    public Flux<ChatModel> findChatsByRoomId(String roomId) {
        return roomRepository.existsById(roomId)
                .filter(exists -> exists)
                .flatMapMany(exists -> chatRepository.findByRoomId(roomId));
    }

    @Override
    public Flux<RoomModel> findAll() {
        return roomRepository.findAll();
    }

    @Override
    public Mono<Long> count() {
        return roomRepository.count();
    }

    @Override
    public Flux<ServerSentEvent<ChatDto>> subscribeByRoomId(String roomId) {
        return chatSinks.computeIfAbsent(roomId, id -> {
                    Sinks.Many<ServerSentEvent<ChatDto>> sink = Sinks.many().replay().all();
                    chatRepository.findByRoomId(roomId)
                            .flatMap(chat -> Flux.just(
                                    ServerSentEvent.builder(
                                                    ChatDto.builder()
                                                            .id(String.valueOf(chat.getId()))
                                                            .roomId(chat.getRoomId())
                                                            .senderId(chat.getSenderId())
                                                            .senderName(chat.getSenderName())
                                                            .message(chat.getMessage())
                                                            .createdAt(chat.getCreatedAt())
                                                            .build())
                                            .build()))
                            .subscribe(sink::tryEmitNext);
                    return sink;
                })
                .asFlux()
                .doOnCancel(() -> {
                    log.info("Unsubscribed room {}", roomId);
                })
                .doOnError(error -> {
                    log.error("Error in room {}", roomId, error);
                    chatSinks.get(roomId).tryEmitError(new ChatException(error.getMessage()));
                })
                .doOnComplete(() -> {
                    log.info("Complete room {}", roomId);
                    chatSinks.get(roomId).tryEmitComplete();
                    chatSinks.remove(roomId);
                });
    }

    @Override
    public Mono<Integer> countConnection() {
        return Mono.just(chatSinks.size());
    }
}