package puga_tmsk.puga_bot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity(name = "productList")
public class ShoppingList {

    @Id
    @GeneratedValue
    private Long id;
    private Long chatId;
    private String product;

    public ShoppingList(Long chatId, String product) {
        this.chatId = chatId;
        this.product = product;
    }

    public ShoppingList() {
        this.chatId = 0L;
        this.product = "";
    }
}
