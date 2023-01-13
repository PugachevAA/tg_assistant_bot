package puga_tmsk.puga_bot.service;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import puga_tmsk.puga_bot.config.BotConfig;
import puga_tmsk.puga_bot.config.BotStatus;
import puga_tmsk.puga_bot.model.*;
import puga_tmsk.puga_bot.service.apps.MonthlyPaymentsApp;
import puga_tmsk.puga_bot.service.apps.ShoppingListApp;
import puga_tmsk.puga_bot.service.apps.UserActionsApp;
import puga_tmsk.puga_bot.service.apps.WishListApp;
import puga_tmsk.puga_bot.service.keyboards.InLineKeyboards;
import puga_tmsk.puga_bot.service.keyboards.ReplyKeyboards;
import puga_tmsk.puga_bot.service.updateHandlers.MainTextHandler;

import java.util.*;

@Slf4j
@Component
@Getter
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    private final UserActionsApp userActionsApp;
    private final ShoppingListApp shoppingListApp;
    private final MonthlyPaymentsApp monthlyPaymentsApp;
    private final WishListApp wishListApp;

    private final ReplyKeyboards replyKeyboards;
    private final InLineKeyboards inLineKeyboards;

    private final MainTextHandler mainTextHandler;

    private final String START_TEXT = "Привет и добро пожаловать! \n\nЯ твой личный ассистент, " +
            "буду хранить список покупок для похода в магазин и ежемесячные платежи :) \n\nА в ближайшем будущем научусь" +
            " хранить вишлисты, TODO, список подписок и наверняка еще что нибудь ;)";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShoppingListRepository shoppingListRepository;
    @Autowired
    private UserSettingsRepository userSettingsRepository;
    @Autowired
    private MonthlyPaymentsRepository monthlyPaymentsRepository;
    @Autowired
    private WishListsRepository wishListsRepository;
    @Autowired
    private WishListItemsRepository wishListItemsRepository;

    public TelegramBot(BotConfig config) {

        userActionsApp = new UserActionsApp(this);
        shoppingListApp = new ShoppingListApp(this);
        monthlyPaymentsApp = new MonthlyPaymentsApp(this);
        wishListApp = new WishListApp(this);

        replyKeyboards = new ReplyKeyboards();
        inLineKeyboards = new InLineKeyboards();

        mainTextHandler = new MainTextHandler(this);

        this.config = config;
        List<BotCommand> menu = new ArrayList<>();
        menu.add(new BotCommand("/main", "ГЛАВНОЕ МЕНЮ"));
        menu.add(new BotCommand("/mydata", "Данные обо мне"));
        menu.add(new BotCommand("/help", "Помощь"));

        try {
            this.execute(new SetMyCommands(menu, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        Message msg;

        String messageText;
        String userName;
        long chatId;
        BotStatus botStatus;

        if (update.hasMessage()) {

            msg = update.getMessage();
            mainTextHandler.mesageHandler(msg);

        } else if (update.hasCallbackQuery()) {

            msg = update.getCallbackQuery().getMessage();

            messageText = update.getCallbackQuery().getData();
            chatId = msg.getChatId();
            userName = msg.getChat().getUserName();
            botStatus = getBotStatus(msg);

            log.info(userName + ": " + messageText);
            if (botStatus == BotStatus.SHOPPING_LIST_ADD) {

                if (messageText.contains("/shoplistendadd")) {
                    sendMessage(msg, "Твой список покупок:",BotStatus.SHOPPING_LIST,
                            inLineKeyboards.getShoppingList(chatId, shoppingListRepository));
                }

                if (messageText.contains("/shoppinglist_")) {
                    String[] itemId = messageText.split("_");
                    shoppingListRepository.deleteById(Long.valueOf(itemId[1]));
                    sendMessage(msg, "Вводи товары по одному, как закончишь нажми Закончить",BotStatus.SHOPPING_LIST_ADD,
                            inLineKeyboards.getShoppingListAdd(chatId, shoppingListRepository));

                }

            } else if (botStatus == BotStatus.MONTHLY_PAYMENTS_ADD_NAME || botStatus == BotStatus.MONTHLY_PAYMENTS_ADD_PRICE) {

                if (messageText.contains("/monthly_payments_add_cancel")) {
                    monthlyPaymentsApp.cancelAddItem(chatId);
                    sendMessage(msg, "Ежемесячные платежи:", BotStatus.MONTHLY_PAYMENTS,
                            inLineKeyboards.getMonthlyPayments(chatId, monthlyPaymentsRepository));
                }
            } else if (botStatus == BotStatus.WISH_LIST_ITEM_ADD_LINK) {
                if (messageText.contains("/wishlist_") && messageText.contains("_item_") && messageText.contains("_cancel")) {
                    wishListApp.cancelAddWishListItemLink(msg, messageText);
                }
            } else {

                if (messageText.contains("/shoppinglist_")) {
                    String[] itemId = messageText.split("_");
                    shoppingListRepository.deleteById(Long.valueOf(itemId[1]));
                    editMessage(msg, "", BotStatus.SHOPPING_LIST,
                            inLineKeyboards.getShoppingList(chatId, shoppingListRepository));
                }

                if (messageText.contains("/monthly_payments_item_view_")) {
                    String[] itemId = messageText.split("_");
                    long id = Long.parseLong(itemId[4]);
                    editMessage(msg, monthlyPaymentsRepository.findById(id).get().getTitle() + ", "
                                    + monthlyPaymentsRepository.findById(id).get().getPrice(),
                                    BotStatus.MONTHLY_PAYMENTS, inLineKeyboards.getMonthlyPaymentsEdit(id));
                }

                //открыть вишлист
                if (messageText.contains("wishlist_") && !messageText.contains("_item")) {
                    String[] listId = messageText.split("_");
                    long wishListId = Long.parseLong(listId[1]);
                    editMessage(msg, "Вишлист: " + wishListsRepository.findById(wishListId).get().getTitle(),
                            BotStatus.WISH_LIST_ITEMS, inLineKeyboards.getWishListMenu(wishListId, wishListItemsRepository));
                }

                //добавить в вишлист
                if (messageText.contains("wishlist_") && messageText.contains("_items_add")) {
                    String[] listId = messageText.split("_");
                    long wishListId = Long.parseLong(listId[1]);
                    wishListApp.addWishListItemMode(msg, wishListId);
                }

                //открыть пункт вишлиста
                if (messageText.contains("wishlist_") && messageText.contains("_item_")
                        && !messageText.contains("_delete") && !messageText.contains("_addlink")) {
                    String[] listId = messageText.split("_");
                    long wishListId = Long.parseLong(listId[1]);
                    long itemId = Long.parseLong(listId[3]);

                    editMessage(msg, wishListItemsRepository.findById(itemId).get().getTitle(),
                            BotStatus.WISH_LIST_ITEMS, inLineKeyboards.getWishListItemMenu(wishListId, itemId, wishListItemsRepository));
                }

                //добавить ссылку
                if (messageText.contains("wishlist_") && messageText.contains("_item_")
                        && messageText.contains("_addlink")) {
                    wishListApp.addWishListItemLinkMode(msg, messageText);
                }

                //удалить пункт вишлиста
                if (messageText.contains("wishlist_") && messageText.contains("_item_")
                        && messageText.contains("_delete")) {
                    wishListApp.deleteWishListItem(msg, messageText);
                }



                if (messageText.contains("/monthly_payments_item_delete_")) {
                    String[] itemId = messageText.split("_");
                    long id = Long.parseLong(itemId[4]);
                    monthlyPaymentsApp.deleteItem(id);
                    editMessage(msg, "Ежемесячные платежи",BotStatus.MONTHLY_PAYMENTS,
                            inLineKeyboards.getMonthlyPayments(chatId, monthlyPaymentsRepository));
                }

                switch (messageText) {
                    case "/main":
                        editMessage(msg, "Главное меню", BotStatus.MAIN, inLineKeyboards.getMain());
                        break;
                    case "/lists":
                        editMessage(msg, "Списки", BotStatus.MAIN, inLineKeyboards.getLists());
                        break;
                    case "/monthly_payments":
                        editMessage(msg, "Ежемесячные платежи", BotStatus.MONTHLY_PAYMENTS,
                                inLineKeyboards.getMonthlyPayments(chatId, monthlyPaymentsRepository));
                        break;
                    case "/monthly_payments_add":
                        editMessage(msg, "Введи название платежа", BotStatus.MONTHLY_PAYMENTS_ADD_NAME,
                                inLineKeyboards.getMonthlyPaymentsAdd());
                        break;
                    case "/shoppinglist":
                        editMessage(msg, "Сходить в магазин", BotStatus.SHOPPING_LIST,
                                inLineKeyboards.getShoppingList(chatId, shoppingListRepository));
                        break;
                    case "/shoplistadditems":
                        editMessage(msg, "Вводи и отправляй покупки по одному сообщению, в конце нажми Закончить",
                                BotStatus.SHOPPING_LIST_ADD, inLineKeyboards.getShoppingListAdd(chatId, shoppingListRepository));
                        break;
                    case "/wishlists_menu":
                        editMessage(msg, "Список вишлистов",
                            BotStatus.WISH_LISTS, inLineKeyboards.getWishLists(chatId, wishListsRepository));
                        break;
                    case "/wishlists_add":
                        editMessage(msg, "Введи название списка:",
                                BotStatus.WISH_LIST_ADD, inLineKeyboards.getCancelMenu("/wishlists"));
                        break;
                    }
                }
        }
    }

    public void editMessage(Message msg, String newText, BotStatus bs, InlineKeyboardMarkup mainInLineKeyboard) {

        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setReplyMarkup(mainInLineKeyboard);
        editMessageReplyMarkup.setChatId(String.valueOf(msg.getChatId()));
        editMessageReplyMarkup.setMessageId(msg.getMessageId());

        try {
            if (!newText.equals("")) {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(String.valueOf(msg.getChatId()));
                editMessageText.setMessageId(msg.getMessageId());
                editMessageText.setText(newText);
                execute(editMessageText);
                log.info("Edit message");

            }
            execute(editMessageReplyMarkup);
            setBotStatus(msg, bs);
            log.info("Edit message");
        }
        catch (TelegramApiException e) {
            log.error("edit error:" + e);
        }
    }

    public BotStatus getBotStatus(Message msg) {
        BotStatus botStatus = BotStatus.MAIN;

        if (!userSettingsRepository.findById(msg.getChatId()).isEmpty()) {
            for (BotStatus bs : BotStatus.values()) {
                if (bs.getId() == userSettingsRepository.findById(msg.getChatId()).get().getBotStatus()) {
                    botStatus = bs;
                }
            }
        }
        return botStatus;
    }


    public void setBotStatus(Message msg, BotStatus bs) {

            UserSettings us = new UserSettings();
            us.setChatId(msg.getChatId());
            us.setBotStatus(bs.getId());
            userSettingsRepository.save(us);
            log.info("setBotStatus: " + bs.getId() + " for user: " + msg.getFrom().getUserName());
    }


    public void startCommandRecieved(Message msg){
        sendMessage(msg, START_TEXT, BotStatus.MAIN, inLineKeyboards.getMain());
    }


    public void sendMessage(Message msg, String textToSend, BotStatus bs, InlineKeyboardMarkup inlineKeyboardMarkup) {
        setBotStatus(msg, bs);
        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(msg.getChatId()));
        sm.setText(textToSend);

        sm.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(sm);
            log.info("ANSWER for User " + msg.getFrom().getUserName());
        }
        catch (TelegramApiException e) {
            log.error("send error:" + e);
        }
    }
}
