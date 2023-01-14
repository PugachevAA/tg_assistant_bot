package puga_tmsk.puga_bot.service.updateHandlers;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;
import puga_tmsk.puga_bot.config.BotStatus;
import puga_tmsk.puga_bot.model.User;
import puga_tmsk.puga_bot.service.TelegramBot;
import puga_tmsk.puga_bot.service.apps.MonthlyPaymentsApp;
import puga_tmsk.puga_bot.service.apps.ShoppingListApp;
import puga_tmsk.puga_bot.service.apps.UserActionsApp;
import puga_tmsk.puga_bot.service.apps.WishListApp;
import puga_tmsk.puga_bot.service.keyboards.InLineKeyboards;

@Slf4j
public class MainTextHandler {

    private final TelegramBot telegramBot;
    private final InLineKeyboards inLineKeyboards;

    private final UserActionsApp userActionsApp;
    private final ShoppingListApp shoppingListApp;
    private final MonthlyPaymentsApp monthlyPaymentsApp;
    private final WishListApp wishListApp;

    private static final String HELP_TEXT = "Это мой тестовый бот. \n\n" +
            "Он уже умеет хранить список покупок для удобного похода в магазин :) \n" +
            "Переходи в меню Список покупок, жми Заполнить, вводи по одному в сообщении, а как заполнишь все пункты жми Закончить. \n" +
            "В магазине просто нажимай на пункт, чтобы он ушел из списка. Удачных покупок :)";


    public MainTextHandler(TelegramBot tgb) {
        telegramBot = tgb;
        inLineKeyboards = new InLineKeyboards();
        userActionsApp = tgb.getUserActionsApp();
        shoppingListApp = tgb.getShoppingListApp();
        monthlyPaymentsApp = tgb.getMonthlyPaymentsApp();
        wishListApp = tgb.getWishListApp();
    }

    public void mesageHandler(Message msg) {
        String messageText;
        String userFirstName;
        long chatId;
        BotStatus botStatus;

        if (msg.hasText()) {


            messageText = msg.getText();
            userFirstName = msg.getChat().getFirstName();
            chatId = msg.getChatId();
            botStatus = telegramBot.getBotStatus(msg);

            switch (messageText) {
                case "/start":
                    userActionsApp.registerUser(msg);
                    telegramBot.startCommandRecieved(msg);
                    break;
                case "/main":
                    telegramBot.clearAllAddStatuses(msg);
                    telegramBot.sendMessage(msg, "Главное меню",BotStatus.MAIN, inLineKeyboards.getMain());
                    break;
                case "/help":
                    telegramBot.clearAllAddStatuses(msg);
                    telegramBot.sendMessage(msg, HELP_TEXT, BotStatus.MAIN, null);
                    break;
                case "/mydata":
                    telegramBot.clearAllAddStatuses(msg);
                    User user = telegramBot.getUserRepository().findById(chatId).get();
                    String userData = user.toString();
                    telegramBot.sendMessage(msg, userData, BotStatus.MAIN, null);
                    break;
                default:
                    if (botStatus == BotStatus.SHOPPING_LIST_ADD) {
                        switch (messageText) {
                            case "/shoplistendadd":
                                ShoppingListApp.endAdd();
                                telegramBot.editMessage(msg, "Сходить в магазин", BotStatus.SHOPPING_LIST,
                                        inLineKeyboards.getShoppingList(chatId, telegramBot.getShoppingListRepository()));
                                break;
                            default:
                                shoppingListApp.addItem(msg);
                        }
                    } else if (botStatus.name().contains("MONTHLY_PAYMENTS_ADD_")) {
                        if (botStatus == BotStatus.MONTHLY_PAYMENTS_ADD_NAME) {
                            monthlyPaymentsApp.addItemName(msg);
                            telegramBot.sendMessage(msg, "Введи сумму платежа:", BotStatus.MONTHLY_PAYMENTS_ADD_PRICE,
                                    inLineKeyboards.getMonthlyPaymentsAdd());
                        } else if (botStatus == BotStatus.MONTHLY_PAYMENTS_ADD_PRICE) {
                            monthlyPaymentsApp.addItemPrice(msg);
                            telegramBot.sendMessage(msg, "Ежемесячные платежи", BotStatus.MONTHLY_PAYMENTS, inLineKeyboards.getMonthlyPayments(chatId, telegramBot.getMonthlyPaymentsRepository()));
                        }
                    } else if (botStatus == BotStatus.WISH_LIST_ADD) {
                        wishListApp.addWishList(msg);
                    } else if (botStatus == BotStatus.WISH_LIST_ITEM_ADD) {
                        wishListApp.addWishListItem(msg);
                    } else if (botStatus == BotStatus.WISH_LIST_ITEM_ADD_LINK) {
                        wishListApp.addWishListItemLink(msg);
                    } else if (messageText.contains("/sendtoall")) {
                        telegramBot.sendMessageForAll(msg);
                    } else {
                            telegramBot.sendMessage(msg, "Чет не то, бро", BotStatus.MAIN, inLineKeyboards.getMain());
                            log.info("MESSAGE: User " + userFirstName + " send command " + messageText);
                    }
            }
        }
    }
}
