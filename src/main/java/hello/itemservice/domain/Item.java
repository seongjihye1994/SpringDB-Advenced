package hello.itemservice.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "item") // DB에 이 Entity 와 매핑할 테이블 이름을 지정할 수 있다. 생략 시 Entity 명으로 생성된다.
public class Item {

    @Id // 해당 컬럼을 Id 필드로 사용하겠다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id 생성 전략을 DB에 위임, 즉 DB가 자동으로 pk를 생성하도록 함.
    private Long id;

    @Column(name = "item_name", length = 10) // 테이블의 컬럼을 지정한다. 생략 시 필드명이 컬럼명이 된다.
    private String itemName;
    private Integer price;
    private Integer quantity;

    // JPA는 public 또는 protected 의 기본 생성자가 필수이다. 기본 생성자를 꼭 넣어주자.
    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}

