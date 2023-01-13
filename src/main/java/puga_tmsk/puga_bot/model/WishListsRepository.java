package puga_tmsk.puga_bot.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WishListsRepository extends CrudRepository<WishLists, Long> {

    List<WishLists> findAllByUserId(long userId);
    WishLists findByUserIdAndAddMode(long userId, boolean addMode);
    List<WishLists> findAllByUserIdAndAddMode(long userId, boolean addMode);
}
