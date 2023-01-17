package puga_tmsk.puga_bot.config;

public enum BotStatus {
    MAIN(0),
    SHOPPING_LIST(1),
    SHOPPING_LIST_ADD(2),
    MONTHLY_PAYMENTS(3),
    MONTHLY_PAYMENTS_ADD(4),
    MONTHLY_PAYMENTS_ADD_NAME(5),
    MONTHLY_PAYMENTS_ADD_PRICE(6),
    WISH_LISTS(7),
    WISH_LIST_ADD(8),
    WISH_LIST_ITEMS(9),
    WISH_LIST_ITEM_ADD(10),
    WISH_LIST_ITEM_ADD_LINK(11),
    TODO(12),
    TODO_LIST_ADD(13),
    TODO_LIST(14),
    TODO_LIST_ITEMS(15),
    TODO_LIST_ITEMS_ADD(16);


    private int id;

    BotStatus(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
