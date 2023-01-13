package puga_tmsk.puga_bot.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WishListItemsRepository extends CrudRepository<WishListItems, Long> {
    List<WishListItems> findAllByWishListId(long wishListId);
    WishListItems findByWishListIdAndAddMode(long wishListId, boolean addMode);
    List<WishListItems> findAllByWishListIdAndAddMode(long wishListId, boolean addMode);
}
