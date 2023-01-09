package puga_tmsk.puga_bot.service.updateHandlers;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import puga_tmsk.puga_bot.config.BotStatus;
import puga_tmsk.puga_bot.model.User;
import puga_tmsk.puga_bot.service.TelegramBot;
import puga_tmsk.puga_bot.service.apps.ShoppingListApp;
import puga_tmsk.puga_bot.service.apps.UserActions;
import puga_tmsk.puga_bot.service.keyboards.InLineKeyboards;

@Slf4j
public class MainTextHandler {

    private TelegramBot telegramBot;
    private final InLineKeyboards inLineKeyboards;
    private UserActions userActions;
    private ShoppingListApp shoppingList;

    private static final String HELP_TEXT = "Это мой тестовый бот. \n\n" +
            "Он уже умеет хранить список покупок для удобного похода в магазин :) \n" +
            "Переходи в меню Список покупок, жми Заполнить, вводи по одному в сообщении, а как заполнишь все пункты жми Закончить. \n" +
            "В магазине просто нажимай на пункт, чтобы он ушел из списка. Удачных покупок :)";


    public MainTextHandler(TelegramBot tgb, UserActions ua, ShoppingListApp sl) {
        telegramBot = tgb;
        inLineKeyboards = new InLineKeyboards();
        userActions = ua;
        shoppingList = sl;
    }

    public void mesageHandler(Update update) {
        String messageText;
        String userFirstName;
        String userName;
        long chatId;
        BotStatus botStatus;
        Message msg;

        if (update.getMessage().hasText()) {

            msg = update.getMessage();
            messageText = update.getMessage().getText();
            userFirstName = update.getMessage().getChat().getFirstName();
            userName = update.getMessage().getChat().getUserName();
            chatId = update.getMessage().getChatId();
            botStatus = telegramBot.getBotStatus(chatId);

            switch (messageText) {
                case "/start":
                    telegramBot.setBotStatus(chatId, userName, BotStatus.MAIN);
                    userActions.registerUser(msg);
                    telegramBot.startCommandRecieved(chatId, userFirstName);
                    break;
                case "/main":
                    telegramBot.setBotStatus(chatId, userName, BotStatus.MAIN);
                    telegramBot.sendMessage(chatId, "Главное меню", "", inLineKeyboards.getMain());
                    break;
                case "/help":
                    telegramBot.sendMessage(chatId, HELP_TEXT, userFirstName, null);
                    break;
                case "/mydata":
                    User user = telegramBot.getUserRepository().findById(chatId).get();
                    String userData = user.toString();
                    telegramBot.sendMessage(chatId, userData, userFirstName, null);
                    break;
                default:
                    if (botStatus == BotStatus.SHOPPING_LIST_ADD) {
                        switch (messageText) {
                            case "/shoplistendadd":
                                ShoppingListApp.endAdd();
                                telegramBot.setBotStatus(chatId, userName, BotStatus.SHOPPING_LIST);
                                telegramBot.editMessage(chatId, msg, "Сходить в магазин", inLineKeyboards.getShoppingList(chatId, messageText, telegramBot.getShoppingListRepository()));
                                break;
                            default:
                                shoppingList.addItem(chatId, messageText);
                        }
                    } else {
                        telegramBot.sendMessage(chatId, "Чет не то, бро","", inLineKeyboards.getMain());
                        log.info("MESSAGE: User " + userFirstName + " send command " + messageText);
                    }
            }
        }
    }
}
