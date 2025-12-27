package _4.NovemberRecipeMarket;

import _4.NovemberRecipeMarket.domain.entity.Item;
import _4.NovemberRecipeMarket.domain.entity.Seller;
import _4.NovemberRecipeMarket.repository.ItemRepository;
import _4.NovemberRecipeMarket.repository.SellerRepository;
import _4.NovemberRecipeMarket.service.IamportClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
@ActiveProfiles("test")
class ItemStockConcurrencyTest {
    @MockBean
    private IamportClient iamportClient;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;


    private TransactionTemplate transactionTemplate;

    private Long itemId;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        itemRepository.deleteAll();
        sellerRepository.deleteAll();

        Seller seller = Seller.builder()
                .username("seller1")
                .password("password")
                .companyName("테스트 회사")
                .businessRegNum("123-45-67890")
                .phoneNumber("010-0000-0000")
                .address("서울시 어딘가")
                .email("seller@test.com")
                .build();
        Seller savedSeller = sellerRepository.save(seller);

        Item item = new Item(savedSeller, "한정판 초콜렛", 10_000, 100);
        Item savedItem = itemRepository.save(item);
        itemId = savedItem.getId();
    }

    @Test
    @DisplayName("동시에 100개의 재고를 차감해도 정상적으로 차감")
    void decreaseStockConcurrently() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    start.await();
                    transactionTemplate.executeWithoutResult(status -> {
                        Item lockedItem = itemRepository.findByIdWithLock(itemId)
                                .orElseThrow();
                        lockedItem.removeStock(1);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await();

        Item itemAfter = itemRepository.findById(itemId).orElseThrow();
        Assertions.assertEquals(0, itemAfter.getStock());
    }
}
