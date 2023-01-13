package puga_tmsk.puga_bot.service.apps;

import org.telegram.telegrambots.meta.api.objects.Message;
import puga_tmsk.puga_bot.config.BotStatus;
import puga_tmsk.puga_bot.model.ShoppingList;
import puga_tmsk.puga_bot.service.TelegramBot;

public class ShoppingListApp {

    TelegramBot telegramBot;
    ShoppingList shoppingList;

    public ShoppingListApp(TelegramBot tgb) {
        telegramBot = tgb;
    }


    public void addItem(Message msg) {
        telegramBot.getShoppingListRepository().save(new ShoppingList(msg.getChatId(), msg.getText()));

        telegramBot.sendMessage(msg, "Вводи товары по одному, как закончишь нажми Закончить", BotStatus.SHOPPING_LIST_ADD,
                                telegramBot.getInLineKeyboards().getShoppingListAdd(msg.getChatId(), telegramBot.getShoppingListRepository()));
    }
    public static void endAdd() {
    }

    public void clear(long chatId) {
        telegramBot.getShoppingListRepository().deleteAll(telegramBot.getShoppingListRepository().findAllByChatId(chatId));
    }
}
