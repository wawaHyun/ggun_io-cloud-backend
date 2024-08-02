package store.ggun.account.repository;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.ggun.account.domain.dto.AccHistoryDto;
import store.ggun.account.domain.model.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.ggun.account.domain.model.Box;

import java.util.List;
import java.util.Optional;


@Repository
public interface AccountRepository extends JpaRepository<AccountModel, Long> {

    List<AccountModel> findByUserId(Long userModel);

    Boolean existsByAcno(String acno);

    @Modifying
    @Query("update accounts set balance = :balance where id = :id")
    int modifyBalanceById(@Param("id") Long id,@Param("balance") Long balance);

    boolean existsByUserIdAndAcType(Long id, String acType);
}
