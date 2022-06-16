package hello.itemservice;

import hello.itemservice.config.JdbcTemplateV3Config;
import hello.itemservice.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;


@Slf4j
//@Import(MemoryConfig.class)
//@Import(JdbcTemplateV1Config.class)
//@Import(JdbcTemplateV2Config.class)
@Import(JdbcTemplateV3Config.class)
@SpringBootApplication(scanBasePackages = "hello.itemservice.web")
public class ItemServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemServiceApplication.class, args);
	}

	@Bean
	@Profile("local")
	public TestDataInit testDataInit(ItemRepository itemRepository) {
		return new TestDataInit(itemRepository);
	}

	/**
	 * 테스트 케이스에만
	 * DataSource 를 직접 스프링 빈으로 등록
	 *
	 * 인메모리 디비 사용 - JVM 내에 DB를 만들고, 그 내부에 데이터를 쌓는다.
	 *
	 * @return
	 */
/*	@Bean
	@Profile("test") // Test 케이스에서만 적용되도록
	public DataSource dataSource() {
		log.info("인메모리 데이터베이스 초기화");
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1"); // 인메모리 디비 사용, 임베디드 모드에서는 데이터베이스 커넥션 연결이 모두 끊어지면 데이터베이스도 종료되는데, 그것을 방지하는 설정
		dataSource.setUsername("sa");
		dataSource.setPassword("sa");
		return dataSource;
	}*/

}
