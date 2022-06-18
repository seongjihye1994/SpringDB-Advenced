package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Transactional // JPA의 모든 데이터 변경은 트랜잭션 안에서 이루어진다.
public class JpaItemRepository implements ItemRepository {

    // JPA를 사용하려면, EntityManager 의존관계를 주입받아야 한다.
    // 이 앤티티 매니저가 SQL을 실행한다.
    private final EntityManager em;

    public JpaItemRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Item save(Item item) {
        em.persist(item); // persist를 하면, @Entity에 있는 매핑 정보들을 가지고, 쿼리를 만들어 DB에 반영한다. 그리고, PK 값도 DB에서 조회 후 @Entity 객체에 저장해준다.
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = em.find(Item.class, itemId);
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
        // em.persist.updqte... 같은거 안해줘도 됨.
        // 컬렉션 저장 하듯 그냥 set... 만 해주면 된다.
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id); // 조회 타입, pk
        return Optional.ofNullable(item); // 조회한 객체가 null 일 수도 있으니 ofNullable 처리
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {

        String jpql = "select i from Item i";

        Integer maxPrice = cond.getMaxPrice();
        String itemName = cond.getItemName();

        if (StringUtils.hasText(itemName) || maxPrice != null) {
            jpql += " where";
        }

        boolean andFlag = false;

        List<Object> param = new ArrayList<>();

        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%',:itemName,'%')";
            param.add(itemName);
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                jpql += " and";
            }

            jpql += " i.price <= :maxPrice";
            param.add(maxPrice);
        }

        log.info("jpql={}", jpql);

        TypedQuery<Item> query = em.createQuery(jpql, Item.class);

        if (StringUtils.hasText(itemName)) {
            query.setParameter("itemName", itemName);
        }

        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }

        return query.getResultList();
    }
}
