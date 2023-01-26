package puga_tmsk.puga_bot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

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

    public String copyOnText(List<WishListItems> wli) {
        List<WishListItems> list = new ArrayList<>();
//        list =
        return "";
    }
}
