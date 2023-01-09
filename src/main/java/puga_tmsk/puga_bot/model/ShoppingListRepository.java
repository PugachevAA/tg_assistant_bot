package puga_tmsk.puga_bot.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ShoppingListRepository  extends CrudRepository<ShoppingList, Long> {

    List<ShoppingList> findAllByChatId(long chatId);

    List<ShoppingList> deleteAllByChatId(long chatId);

}
