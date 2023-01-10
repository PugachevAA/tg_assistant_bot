package puga_tmsk.puga_bot.model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

public class WishList {

    @Id
    @GeneratedValue
    private long id;
    private long userId;
    private String title;
    private String link;
}
