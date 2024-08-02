package store.ggun.alarm.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import store.ggun.alarm.domain.model.PostModel;
import store.ggun.alarm.repository.PostRepository;


@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Mono<PostModel> createPost(PostModel post) {
        return postRepository.save(post);
    }

    public Flux<PostModel> getAllPosts() {
        return postRepository.findAll();
    }

    public Mono<PostModel> getPostById(String id) {
        return postRepository.findById(id);
    }

    public Mono<Void> deletePostById(String id) {
        return postRepository.deleteById(id);
    }
}
