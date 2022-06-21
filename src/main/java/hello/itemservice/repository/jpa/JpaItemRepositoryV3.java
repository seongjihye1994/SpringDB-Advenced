package hello.itemservice.repository.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.domain.QItem;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static hello.itemservice.domain.QItem.item;

@Repository
@Transactional // JPA의 모든 데이터 변경은 트랜잭션 안에서 이루어진다.
public class JpaItemRepositoryV3 implements ItemRepository {

    private final EntityManager em;
    private final JPAQueryFactory query; // JPA 사용을 위한 JPAQueryFactory 주입

    public JpaItemRepositoryV3(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
        // JPAQueryFactory 은 QueryDSL이 지원한다.
        // QueryDSL은 최종적으로, JPA의 JPQL을 만들어주는 빌더 역할을 한다.
        // JPAQueryFactory 는 JPA 쿼리를 만들어 주는 공장이고,
        // 그 내부에 앤티티 매니저를 넣어주면 된다.
    }

    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = em.find(Item.class, itemId);
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    /*public List<Item> findAllOld(ItemSearchCond cond) {

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        // Query dsl 사용하기!
        QItem item = QItem.item; // QueryDSL 사용을 위한 Q타입
        BooleanBuilder builder = new BooleanBuilder(); // 동적쿼리 조건 작성을 위한 빌더 생성

        if (StringUtils.hasText(itemName)) { // 검색값에 itemName이 있다면
            builder.and(item.itemName.like("%" + itemName + "%")); // 빌더에 쌓고
        }

        if (maxPrice != null) { // 검색값에 maxPrice가 있다면
            builder.and(item.price.loe(maxPrice)); // 빌더에 쌓는다.
        }

        List<Item> result = query
                .select(item)
                .from(item)
                .where(builder) // 동적쿼리 조건절
                .fetch();

        return result;
    }*/

    @Override
    public List<Item> findAll(ItemSearchCond cond) {

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        // Query dsl 사용하기!
        return query
                .select(item)
                .from(item)
                .where(likeItemName(itemName), maxPrice(maxPrice)) // 동적쿼리 조건절
                .fetch();
    }

    private BooleanExpression likeItemName(String itemName) {
        if (StringUtils.hasText(itemName)) {
            return item.itemName.like("%" + itemName + "%");
        }
        return null;
    }

    private BooleanExpression maxPrice(Integer maxPrice) {
        if (maxPrice != null) {
            return item.price.loe(maxPrice);
        }
        return null;
    }



}
