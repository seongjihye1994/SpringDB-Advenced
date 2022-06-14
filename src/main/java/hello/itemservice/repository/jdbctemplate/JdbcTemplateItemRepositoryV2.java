package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * NamedParameterJdbcTemplate 사용 - 이름을 지정해서 파라미터를 바인딩 하는 기능
 *
 * SqlParameterSource 사용
 *  - BeanPropertySqlParameterSource
 *  - MapSqlParameterSource
 * Map
 *
 * BeanPropertyRowMapper - rs 의 결과값을 Item dto에 있는 필드명을 기준으로 자동으로 매핑해줌
 */
@Slf4j
public class JdbcTemplateItemRepositoryV2 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;

    // JdbcTemplate 은 datasource가 필요하다.
    // dataSource 를 의존 관계 주입 받고 생성자 내부에서 JdbcTemplate 을 생성
    public JdbcTemplateItemRepositoryV2(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }


    @Override
    public Item save(Item item) {

        String sql = "insert into item(item_name, price, quantity) values (:itemName, :price, :quantity)";

        // 이름 매칭 방법 1
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        // 순서 매칭이 아닌, name 으로 바인딩하기 위해 사용
        // item dto의 필드명과 동일하게 매칭된다.

        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder);

        long key = keyHolder.getKey().longValue();
        item.setId(key);

        return item;

    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {

        String sql = "update item set item_name=:itemName, price=:price, quantity=:quantity where id=:id";

        // 이름 매칭 방법 2
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId);

        template.update(sql, param);
    }

    @Override
    public Optional<Item> findById(Long id) {

        String sql = "select id, item_name, price, quantity from Item where id = :id";

        // queryForObject 는 결과가 없다면 항상 EmptyResultDataAccessException 에러를 터트린다.
        // 그래서 Optional.of() 를 사용해도 된다.
        // 참고로 Optional.of() 는 인자가 null 이면 안된다. 반대는 ofNullable() 이다.
        try {

            // 이름 매칭 방법 3
            Map<String, Object> param = Map.of("id", id);

            Item item = template.queryForObject(sql, param, itemRowMapper());

            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty(); // 쿼리 결과가 없다면 빈 Optional 객체로 리턴
        }
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        SqlParameterSource param = new BeanPropertySqlParameterSource(cond);

        String sql = "select id, item_name, price, quantity from Item";

        // 동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }

        boolean andFlag = false;

        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',:itemName,'%')";
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }

            sql += " price <= :maxPrice";
        }

        log.info("sql={}", sql);

        // query 는 리스트를 가져올 때 사용한다.
        return template.query(sql, param, itemRowMapper());
    }


    private RowMapper<Item> itemRowMapper() {
        return BeanPropertyRowMapper.newInstance(Item.class);
        // rs 의 결과값을 Item dto에 있는 필드명을 기준으로 자동으로 매핑해줌
    }
}
