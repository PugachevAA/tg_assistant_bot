package puga_tmsk.puga_bot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity(name = "wishListItems")
public class WishListItems {

    @Id
    @GeneratedValue
    private long id;
    private long wishListId;
    private String title;
    private String link;
    private boolean addMode;
}
