package puga_tmsk.puga_bot.config;

public enum BotStatus {
    MAIN(0),
    SHOPPING_LIST(1),
    SHOPPING_LIST_ADD(2),
    MONTHLY_PAYMENTS(3),
    MONTHLY_PAYMENTS_ADD(4),
    MONTHLY_PAYMENTS_ADD_NAME(5),
    MONTHLY_PAYMENTS_ADD_PRICE(6);


    private int id;

    BotStatus(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public BotStatus getStatusById(int id) {
        for (BotStatus bs : BotStatus.values()) {
            if (bs.getId() == id) {
                return bs;
            }
        }
        return null;
    }
}
