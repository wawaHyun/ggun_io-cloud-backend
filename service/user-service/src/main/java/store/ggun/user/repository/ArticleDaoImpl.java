package store.ggun.user.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import store.ggun.user.domain.ArticleDto;
import store.ggun.user.domain.QArticleModel;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ArticleDaoImpl implements ArticleDao{
    private final JPAQueryFactory queryFactory;
    private final QArticleModel qArticle = QArticleModel.articleModel;

    public List<ArticleDto> findByBoardIdDao(String boardId) {

        return queryFactory
                .select(Projections.constructor(ArticleDto.class,
                        qArticle.id,
                        qArticle.title,
                        qArticle.content,
                        qArticle.writerId.username,
                        qArticle.boardId,
                        qArticle.answer))
                .from(qArticle)
                .where(qArticle.boardId.eq(boardId))
                .fetch();
    }
}
