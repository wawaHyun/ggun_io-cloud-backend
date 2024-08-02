package store.ggun.user.serviceImpl;

import com.querydsl.core.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import store.ggun.user.domain.ArticleDto;
import store.ggun.user.domain.ArticleModel;
import store.ggun.user.domain.Messenger;
import store.ggun.user.domain.UserModel;
import store.ggun.user.repository.ArticleRepository;
import store.ggun.user.service.ArticleService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository repository;

    @Override
    public ArticleModel save(ArticleDto model, String id) {
        Long writerId = Long.valueOf(id);
        ArticleModel article = ArticleModel.builder()
                .title(model.getTitle())
                .content(model.getContent())
                .writerId(UserModel.builder()
                        .id(writerId)
                        .build())
                .boardId(model.getBoardId())
                .build();
        ArticleModel article1 = repository.save(article);
        return article1;
    }

    @Override
    public List<ArticleDto> findAllByBoardId(String boardId) {
        return repository.findByBoardIdDao(boardId);
    }
}
