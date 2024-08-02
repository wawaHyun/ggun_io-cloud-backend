package store.ggun.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.ggun.user.domain.ArticleModel;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<ArticleModel, Long>, ArticleDao {
}
