package puga_tmsk.puga_bot.service.apps;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import puga_tmsk.puga_bot.config.BotStatus;
import puga_tmsk.puga_bot.model.ShoppingList;
import puga_tmsk.puga_bot.service.TelegramBot;
import puga_tmsk.puga_bot.service.keyboards.InLineKeyboards;

public class ShoppingListApp {

    TelegramBot telegramBot;

    public ShoppingListApp(TelegramBot tgb) {
        telegramBot = tgb;
    }

    public void addItem(Message msg) {
        telegramBot.getShoppingListRepository().save(new ShoppingList(msg.getChatId(), msg.getText()));

        telegramBot.sendMessage(msg, "Вводи товары по одному, как закончишь нажми Закончить", BotStatus.SHOPPING_LIST_ADD,
                                telegramBot.getInLineKeyboards().getShoppingListAdd(msg.getChatId()));
    }

    public void endAdd(Message msg) {
        telegramBot.getMenu().shoppingListMenu(msg, BotStatus.SHOPPING_LIST);
    }

    public void clear(long chatId) {
        telegramBot.getShoppingListRepository().deleteAll(telegramBot.getShoppingListRepository().findAllByChatId(chatId));
    }

    public void deleteItem(Message msg, String messageText) {
        String[] itemId = messageText.split("_");
        telegramBot.getShoppingListRepository().deleteById(Long.valueOf(itemId[1]));
        BotStatus bs = telegramBot.getBotStatus(msg);
        telegramBot.getMenu().shoppingListMenu(msg, bs);
    }
}
