package hello.itemservice.domain;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import hello.itemservice.repository.memory.MemoryItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Transactional // @Transactional 애노테이션을 테스트에서 사용하면 스프링은 테스트를 트랜잭션 안에서 실행하고, 테스트가 끝나면 트랜잭션을 자동으로 롤백시킨다.
@SpringBootTest // @SpringBootApplication 어노테이션을 찾아 해당 어노테이션이 적용된 클래스를 설정으로 사용한다.
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;
/*
    @Autowired
    PlatformTransactionManager transactionManager;
    TransactionStatus status;*/

/*    @BeforeEach
    void beforeEach() {
        // 트랜잭션 시작
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // 트랜잭션 관리자는 PlatformTransactionManager 를 주입 받아서 사용
    }*/

    // @AfterEach 는 각각의 테스트의 실행이 끝나는 시점에 호출된다.
    // 여기서는 메모리 저장소를 완전히 삭제해서 다음 테스트에 영향을 주지 않도록 초기화 한다.
    @AfterEach
    void afterEach() {
        // MemoryItemRepository 의 경우 제한적으로 사용
        if (itemRepository instanceof MemoryItemRepository) {
            ((MemoryItemRepository) itemRepository).clearStore();
        }

        // 트랜잭션 롤백
//        transactionManager.rollback(status);
    }

    @Test
    void save() {
        // given
        Item item = new Item("itemA", 10000, 10);

        // when
        Item savedItem = itemRepository.save(item);

        // then
        Item findItem = itemRepository.findById(item.getId()).get();
        assertThat(findItem).isEqualTo(savedItem);
    }

    @Test
    void updateItem() {

        // given
        Item item = new Item("item1", 10000, 10);
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        // when
        ItemUpdateDto updateParam = new ItemUpdateDto("item2", 20000, 30);
        itemRepository.update(itemId, updateParam);

        // then
        Item findItem = itemRepository.findById(itemId).get();
        assertThat(findItem.getItemName()).isEqualTo(updateParam.getItemName());
        assertThat(findItem.getPrice()).isEqualTo(updateParam.getPrice());
        assertThat(findItem.getQuantity()).isEqualTo(updateParam.getQuantity());
    }

    @Test
    void findItems() {

        // given
        Item item1 = new Item("itemA-1", 10000, 10);
        Item item2 = new Item("itemA-2", 20000, 20);
        Item item3 = new Item("itemB-1", 30000, 30);

        log.info("repository={}", itemRepository.getClass()); // AOP 프록시 생성됨을 확인
        // JPA와 스프링은 별개이다.
        // 따라서, 예외도 별개이다.
        // JPA 엔티티 매니저에서 예외가 발생하면
        // @Repository와 @Transactional 어노테이션이 있다면
        // 예외 변환 AOP 프록시를 생성하고,
        // 예외 변환 AOP 프록시에서 JPA 예외를 스프링 예외로 변환한 후
        // 서비스 로직으로 예외를 던진다.


        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        // 둘 다(itemName, maxPrice) 없음 검증
        test(null, null, item1, item2, item3);
        test("", null, item1, item2, item3);

        // itemName 으로 검증
        test("itemA", null, item1, item2);
        test("temA", null, item1, item2); // 부분 검색
        test("itemB", null, item3);

        // maxPrice 으로 검증
        test(null, 10000, item1);

        // 둘 다(itemName, maxPrice) 있음 검증
        test("itemA", 10000, item1);
    }

    void test(String itemName, Integer maxPrice, Item... items) {
        List<Item> result = itemRepository.findAll(new ItemSearchCond(itemName, maxPrice));
        assertThat(result).containsExactly(items);
    }
}
