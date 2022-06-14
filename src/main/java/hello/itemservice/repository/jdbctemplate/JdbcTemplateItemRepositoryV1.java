package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JdbcTemplate 사용
 */
@Slf4j
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

    private final JdbcTemplate template;

    // JdbcTemplate 은 datasource가 필요하다.
    // dataSource 를 의존 관계 주입 받고 생성자 내부에서 JdbcTemplate 을 생성
    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }


    @Override
    public Item save(Item item) {

        String sql = "insert into item(item_name, price, quantity) values (?,?,?)";

        // 데이터를 저장할 때 PK 생성에 identity (auto increment) 방식 사용 -> DB가 pk를 대신 생성해줌
        // 문제는 DB가 대신 생성해주는 Pk ID 값은 디비가 생성하기 때문에,
        // 디비에 INSERT 쿼리가 완료되어야 생성된 PK ID 값을 확인할 수 있음
        // 그래서 keyholder 와 connection.preparedStatement 를 사용해서
        // id를 지정해주면 INSERT 쿼리 실행 이후에 데이터베이스에서 생성된 id 값을 조회할 수 있다.
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            // 자동 증가 키인 경우 이렇게 작성해야 한다.
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, item.getItemName());
            ps.setInt(2, item.getPrice());
            ps.setInt(3, item.getQuantity());
            return ps;
        }, keyHolder);

        long key = keyHolder.getKey().longValue();
        item.setId(key);

        return item;

    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {

        String sql = "update item set item_name=?, price=?, quantity=? where id=?";

        template.update(sql,
                updateParam.getItemName(),
                updateParam.getPrice(),
                updateParam.getQuantity(),
                itemId);
    }

    @Override
    public Optional<Item> findById(Long id) {

        String sql = "select id, item_name, price, quantity from Item where id = ?";

        // queryForObject 는 결과가 없다면 항상 EmptyResultDataAccessException 에러를 터트린다.
        // 그래서 Optional.of() 를 사용해도 된다.
        // 참고로 Optional.of() 는 인자가 null 이면 안된다. 반대는 ofNullable() 이다.
        try {
            Item item = template.queryForObject(sql, itemRowMapper(), id);
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty(); // 쿼리 결과가 없다면 빈 Optional 객체로 리턴
        }
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        String sql = "select id, item_name, price, quantity from Item where id = ?";

        // 동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }

        boolean andFlag = false;
        List<Object> param = new ArrayList<>();

        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',?,'%')";
            param.add(itemName);
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }

            sql += " price <= ?";
            param.add(maxPrice);
        }

        log.info("sql={}", sql);

        // query 는 리스트를 가져올 때 사용한다.
        return template.query(sql, itemRowMapper(), param.toArray());
    }


    private RowMapper<Item> itemRowMapper() {
        return((rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        });
    }
}
