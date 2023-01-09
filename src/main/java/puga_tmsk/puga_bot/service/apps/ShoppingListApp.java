package puga_tmsk.puga_bot.service.apps;

import puga_tmsk.puga_bot.model.ShoppingList;
import puga_tmsk.puga_bot.service.TelegramBot;

public class ShoppingListApp {

    TelegramBot telegramBot;
    ShoppingList shoppingList;

    public ShoppingListApp(TelegramBot tgb) {
        telegramBot = tgb;
    }


    public void addItem(Long chatId, String messageText) {

        shoppingList = new ShoppingList(chatId, messageText);

        telegramBot.getShoppingListRepository().save(shoppingList);
        telegramBot.sendMessage(chatId, "Вводи товары по одному, как закончишь нажми Закончить", "",
                                telegramBot.getInLineKeyboards().getShoppingListAdd(chatId, messageText, telegramBot.getShoppingListRepository()));
    }
    public static void endAdd() {
    }

    public void clear(long chatId) {
        telegramBot.getShoppingListRepository().deleteAll(telegramBot.getShoppingListRepository().findAllByChatId(chatId));
    }
}
