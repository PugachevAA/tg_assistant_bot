package puga_tmsk.puga_bot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity(name = "productList")
public class ShoppingList {

    @Id
    private Long id;
    private Long chatId;
    private String product;
}
