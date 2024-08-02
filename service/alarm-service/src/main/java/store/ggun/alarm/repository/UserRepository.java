package store.ggun.alarm.repository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import store.ggun.alarm.domain.model.UserModel;


@Repository
public interface UserRepository extends ReactiveMongoRepository<UserModel, String> {
    Flux<UserModel> findByLastName(String lastName);
    Flux<UserModel> findAll();
    Mono<UserModel> findByEmail(String email);
}
