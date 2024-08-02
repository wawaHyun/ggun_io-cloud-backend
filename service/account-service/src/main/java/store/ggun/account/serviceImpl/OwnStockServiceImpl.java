package store.ggun.account.serviceImpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import store.ggun.account.domain.dto.AccountDto;
import store.ggun.account.domain.dto.Messenger;
import store.ggun.account.domain.dto.OwnStockDto;
import store.ggun.account.domain.dto.TradeDto;
import store.ggun.account.domain.model.AccountModel;
import store.ggun.account.domain.model.OwnStockModel;
import store.ggun.account.repository.AccountRepository;
import store.ggun.account.repository.OwnStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import store.ggun.account.repository.TradeDao;
import store.ggun.account.service.AccountService;
import store.ggun.account.service.OwnStockService;
import store.ggun.account.service.TradeService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnStockServiceImpl implements OwnStockService {

    private final OwnStockRepository ownStockRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final TradeService tradeService;
    private final KISOpenFeign openFeign;
//    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("emf"); //엔티티 매니저 팩토리 생성
//    private final EntityManager em = emf.createEntityManager(); //엔티티 매니저 생성
//    private final EntityTransaction tx = em.getTransaction();

    @Override
    public List<OwnStockDto> findByAccount(Long id) {
        return ownStockRepository.findByAccountId(id).stream().map(i->entityToDto(i)).toList();
    }

    @Override
    @Transactional
    public Messenger save(OwnStockDto ownStockDto) {
        Optional<OwnStockModel> stock = ownStockRepository.findByPdnoAndAccountIdAndTradeType(ownStockDto.getPdno(), ownStockDto.getAccount(), ownStockDto.getTradeType());
        Optional<AccountModel> account = accountRepository.findById(ownStockDto.getAccount());

        long totalOrderAmount = ownStockDto.getPdQty() * ownStockDto.getAvgPrvs();

        long totalPurchaseAmount = stock.isEmpty() ? 0L : stock.get().getPdQty() * stock.get().getAvgPrvs();

        long totalPdQty = 0L;
        int result = 0;
        String msg = "";
        String tradeHistory ="";

        if (ownStockDto.getSllBuyDvsnCd() == 1) {

            AccountDto acDto = AccountDto.builder()
                    .id(account.get().getId())
                    .balance(totalOrderAmount)
                    .tradeType("주식매수출금")
                    .briefs(ownStockDto.getPrdtName() + " 매수")
                    .build();
             msg = accountService.withdraw(acDto).getMessage();
            if (msg.equals("SUCCESS")) {
                if (stock.isEmpty()) {
                    ownStockRepository.save(OwnStockModel.builder()
                            .pdno(ownStockDto.getPdno())
                            .prdtName(ownStockDto.getPrdtName())
                            .pdQty(ownStockDto.getPdQty())
                            .avgPrvs(ownStockDto.getAvgPrvs())
                            .tradeType(ownStockDto.getTradeType())
                            .account(account.get())
                            .build());

                    TradeDto tradeDto = TradeDto.builder()
                            .ordDvsnName(ownStockDto.getOrdDvsnCd()==1?"시장가":"지정가")
                            .ordDvsnCd(ownStockDto.getOrdDvsnCd())
                            .sllBuyDvsnCd(ownStockDto.getSllBuyDvsnCd())
                            .pdno(ownStockDto.getPdno())
                            .prdtName(ownStockDto.getPrdtName())
                            .ordQty(ownStockDto.getPdQty())
//                            .totCcldQty()
                            .ccldPrvs(ownStockDto.getAvgPrvs())
                            .standardFee(0.015)
                            .baseTax(0.02)
                            .account(ownStockDto.getAccount())
                            .build();

                    tradeHistory = tradeService.save(tradeDto).getMessage();

                    return Messenger.builder().message(msg).build();

                } else {
                    totalPdQty = stock.get().getPdQty() + ownStockDto.getPdQty();
                    long avgPrvs = (totalOrderAmount + totalPurchaseAmount) / totalPdQty;

//                    stock.get().setPdQty(totalPdQty);
//                    stock.get().setAvgPrvs(avgPrvs);
//                    ownStockRepository.save(stock.get());
                    TradeDto tradeDto = TradeDto.builder()
                            .ordDvsnName(ownStockDto.getOrdDvsnCd()==1?"시장가":"지정가")
                            .ordDvsnCd(ownStockDto.getOrdDvsnCd())
                            .sllBuyDvsnCd(ownStockDto.getSllBuyDvsnCd())
                            .pdno(ownStockDto.getPdno())
                            .prdtName(ownStockDto.getPrdtName())
                            .ordQty(ownStockDto.getPdQty())
//                            .totCcldQty()
                            .ccldPrvs(ownStockDto.getAvgPrvs())
                            .standardFee(0.015)
                            .baseTax(0.02)
                            .account(ownStockDto.getAccount())
                            .build();
                    tradeHistory = tradeService.save(tradeDto).getMessage();
                     result = ownStockRepository.modifyStock(stock.get().getId(), totalPdQty,avgPrvs);

                    return Messenger.builder().message(result==1 ? msg :"FAIURE").build();
                }
            }else {
                return Messenger.builder().message(msg).build();
            }
        } else if (ownStockDto.getSllBuyDvsnCd() == 2) {
            if (stock.isEmpty()) {

                return Messenger.builder().message("종목을 보유하고 있지 않습니다.").build();

            } else {
                totalPdQty = stock.get().getPdQty() - ownStockDto.getPdQty();

//                stock.get().setPdQty(totalPdQty);
//                stock.get().setAvgPrvs( totalPurchaseAmount / totalPdQty);

                if (totalPdQty == 0) {
                    result = ownStockRepository.deleteByPdnoAndAccountId(ownStockDto.getPdno(), ownStockDto.getAccount());
                }else {
                    result = ownStockRepository.modifyStock(stock.get().getId(), totalPdQty);
                }
                String userAc = "";
                String adminAc = "";
                if(result ==1 ) {
                    long fee = Math.round(totalOrderAmount*0.00015);
                    long tax = Math.round(totalOrderAmount*0.002);
                    long netIncome = totalOrderAmount - fee-tax;
                    AccountDto UserAcDto = AccountDto.builder()
                            .id(account.get().getId())
                            .balance(netIncome)
                            .tradeType("주식매도입금")
                            .briefs(ownStockDto.getPrdtName() + " 매도")
                            .build();
                    AccountDto adimAcDto = AccountDto.builder()
                            .id(1L)
                            .balance(fee)
                            .tradeType("입금")
                            .briefs("사용자 매도수수료입금")
                            .build();
                    TradeDto tradeDto = TradeDto.builder()
                            .ordDvsnName(ownStockDto.getOrdDvsnCd()==1?"시장가":"지정가")
                            .ordDvsnCd(ownStockDto.getOrdDvsnCd())
                            .sllBuyDvsnCd(ownStockDto.getSllBuyDvsnCd())
                            .pdno(ownStockDto.getPdno())
                            .prdtName(ownStockDto.getPrdtName())
                            .ordQty(ownStockDto.getPdQty())
//                            .totCcldQty()
                            .ccldPrvs(ownStockDto.getAvgPrvs())
                            .sellingFee(fee)
                            .sellingTax(tax)
                            .standardFee(0.015)
                            .baseTax(0.02)
                            .account(ownStockDto.getAccount())
                            .build();

                    userAc = accountService.deposit(UserAcDto).getMessage();
                    adminAc = accountService.deposit(adimAcDto).getMessage();
                    tradeHistory = tradeService.save(tradeDto).getMessage();

                }
                return Messenger.builder().message(userAc.equals("SUCCESS")
                        &&adminAc.equals("SUCCESS")
                        &&tradeHistory.equals("SUCCESS")
                        ? "주문(매도) 완료": "FAIURE").build();
            }
        }
        return Messenger.builder().message("매도/매수 선택").build();
    }

    @Override
    public Messenger deleteById(Long id) {
        return null;
    }

    @Override
    public Optional<OwnStockDto> modify(OwnStockDto ownStockDto) {
        return Optional.empty();
    }

    @Override
    public List<OwnStockDto> findAll() {
        return null;
    }

    @Override
    public Optional<OwnStockDto> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public long count() {
        return ownStockRepository.count();
    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }


}
