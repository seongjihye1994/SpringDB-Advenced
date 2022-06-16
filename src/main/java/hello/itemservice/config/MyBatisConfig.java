package hello.itemservice.config;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.jdbctemplate.JdbcTemplateItemRepositoryV3;
import hello.itemservice.repository.mybatis.ItemMapper;
import hello.itemservice.repository.mybatis.MyBatisItemRepository;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration // 설정
@RequiredArgsConstructor
public class MyBatisConfig {

    private final ItemMapper itemMapper;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new MyBatisItemRepository(itemMapper);
    }


}

/**
 * ## 동적 SQL
 *
 * - *** if ***
 * - *** choose (when, otherwise) ***
 * - *** trim (where, set) ***
 * - *** foreach ***
 *
 * choose 는 자바의 switch 구문과 유사하다.
 *
 * WHERE 문을 언제 넣어야 할지 상황에 따라서 동적으로 달라지는 문제가 있다.
 *
 * 이 때는 <where> 를 사용하면 이런 문제를 해결할 수 있다.
 *
 * <where> 는 문장이 없으면 where 를 추가하지 않는다.
 *
 * 문장이 있으면 where 를 추가한다.
 *
 * 만약 and 가 먼저 시작된다면 and 를 지운다.
 *
 * foreach는 컬렉션을 반복 처리할 때 사용한다.
 *
 * where in (1,2,3,4,5,6) 와 같은 문장을 쉽게 완성할 수 있다.
 *
 * 파라미터로 List 를 전달하면 된다.
 */
