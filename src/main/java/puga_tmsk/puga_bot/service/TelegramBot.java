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
import puga_tmsk.puga_bot.service.apps.*;
import puga_tmsk.puga_bot.service.keyboards.InLineKeyboards;
import puga_tmsk.puga_bot.service.keyboards.ReplyKeyboards;
import puga_tmsk.puga_bot.service.updateHandlers.MainTextHandler;

import java.util.*;

@Slf4j
@Component
@Getter
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    final Menu menu;

    private final UserActionsApp userActionsApp;
    private final ShoppingListApp shoppingListApp;
    private final MonthlyPaymentsApp monthlyPaymentsApp;
    private final WishListApp wishListApp;
    private final ToDoApp toDoApp;

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
    @Autowired
    private ToDoListRepository toDoListRepository;
    @Autowired
    private ToDoListItemRepository toDoListItemRepository;

    public TelegramBot(BotConfig config) {

        menu = new Menu(this);

        userActionsApp = new UserActionsApp(this);
        shoppingListApp = new ShoppingListApp(this);
        monthlyPaymentsApp = new MonthlyPaymentsApp(this);
        wishListApp = new WishListApp(this);
        toDoApp = new ToDoApp(this);

        replyKeyboards = new ReplyKeyboards();
        inLineKeyboards = new InLineKeyboards(this);

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
                    shoppingListApp.endAdd(msg);
                }

                if (messageText.contains("/shoppinglist_")) {
                    shoppingListApp.deleteItem(msg, messageText);
                }

            } else if (botStatus == BotStatus.MONTHLY_PAYMENTS_ADD_NAME || botStatus == BotStatus.MONTHLY_PAYMENTS_ADD_PRICE) {

                if (messageText.contains("/monthly_payments_add_cancel")) {
                    monthlyPaymentsApp.cancelAddItem(msg);
                    menu.monthlyPaymentsMenu(msg);
                }
            } else if (botStatus == BotStatus.WISH_LIST_ITEM_ADD_LINK) {
                if (messageText.contains("/wishlist_") && messageText.contains("_item_") && messageText.contains("_cancel")) {
                    wishListApp.cancelAddWishListItemLink(msg, messageText);
                }
            } else {

                //удалить покупку
                if (messageText.contains("/shoppinglist_")) {
                    shoppingListApp.deleteItem(msg, messageText);
                }

                //открыть пункт ежемесячного платежа
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
                            BotStatus.WISH_LIST_ITEMS, inLineKeyboards.getWishListMenu(wishListId));
                }

                //скопировать вишлист
                if (messageText.contains("wishlist_") && messageText.contains("_copy") && !messageText.contains("item")) {
                    wishListApp.copyWishList(msg, messageText);
                }

                //удалить вишлист
                if (messageText.contains("wishlist_") && messageText.contains("_delete") && !messageText.contains("item")) {
                    wishListApp.deleteWishList(msg, messageText);
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
                            BotStatus.WISH_LIST_ITEMS, inLineKeyboards.getWishListItemMenu(wishListId, itemId));
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

                //удалить пункт Ежемесячного платежа
                if (messageText.contains("/monthly_payments_item_delete_")) {
                    String[] itemId = messageText.split("_");
                    long id = Long.parseLong(itemId[4]);
                    monthlyPaymentsApp.deleteItem(id);
                    editMessage(msg, "Ежемесячные платежи",BotStatus.MONTHLY_PAYMENTS,
                            inLineKeyboards.getMonthlyPayments(chatId));
                }

                //открыть to do лист
                if (messageText.contains("/todo_list_") && !messageText.contains("/_delete") &&
                    !messageText.contains("_add") && !messageText.contains("_item_")) {
                    toDoApp.openToDoList(msg, messageText);
                }

                //удалить to do лист
                if (messageText.contains("todo_list_") && messageText.contains("_delete") &&
                    !messageText.contains("_item")) {
                    toDoApp.deleteTodoList(msg, messageText);
                }

                //добавить в to do лист
                if (messageText.contains("todo_list_") && messageText.contains("_items_add")) {
                    String[] listId = messageText.split("_");
                    long todoListId = Long.parseLong(listId[2]);
                    toDoApp.addTodoListItemMode(msg, todoListId);
                }

                //закончить добавление в to do лист
                if (messageText.contains("/todo_list_") && messageText.contains("_endadd")) {
                    String[] listId = messageText.split("_");
                    long todoListId = Long.parseLong(listId[2]);
                    toDoApp.stopTodoListItemsAdd(msg, todoListId);
                }

                //удалить из to do листа пункт
                if (messageText.contains("/todo_list_") && messageText.contains("_item_")) {
                    String[] listId = messageText.split("_");
                    long todoListId = Long.parseLong(listId[2]);
                    long itemId = Long.parseLong(listId[4]);
                    toDoApp.deleteTodoListItem(msg, todoListId, itemId);
                }

                switch (messageText) {
                    case "/main":
                        menu.main(msg);
                        break;
                    case "/lists":
                        menu.listsMenu(msg);
                        break;
                    case "/monthly_payments":
                        menu.monthlyPaymentsMenu(msg);
                        break;
                    case "/monthly_payments_add":
                        menu.monthlyPaymentsAddMenu(msg);
                        break;
                    case "/shoppinglist":
                        menu.shoppingListMenu(msg, BotStatus.SHOPPING_LIST);
                        break;
                    case "/shoplistadditems":
                        menu.shoppingListAddMenu(msg);
                        break;
                    case "/wishlists_menu":
                        menu.wishListsMenu(msg);
                        break;
                    case "/wishlists_add":
                        menu.wishListAddMenu(msg);
                        break;
                    case "/todo_lists_menu":
                        menu.toDoLists(msg);
                        break;
                    case "/todo_list_add":
                        menu.toDoListAddMenu(msg);
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
        log.info("setBotStatus: " + bs.getId() + " for user: " + msg.getChat().getUserName());



    }
    public void clearAllAddStatuses(Message msg) {
        BotStatus bs = getBotStatus(msg);
        if (bs == BotStatus.WISH_LIST_ADD) {
            List<WishLists> wls = wishListsRepository.findAllByUserIdAndAddMode(msg.getFrom().getId(), true);
            for (WishLists wl: wls) {
                wl.setAddMode(false);
            }
            wishListsRepository.saveAll(wls);
        }
        if (bs == BotStatus.WISH_LIST_ITEM_ADD_LINK) {
            List<WishListItems> wlis = new ArrayList<>();
            List<WishLists> wls = wishListsRepository.findAllByUserId(msg.getFrom().getId());
            for (WishLists wl: wls) {
                wl.setAddMode(false);
                wlis.addAll(wishListItemsRepository.findAllByWishListId(wl.getId()));
            }
            for (WishListItems wli : wlis) {
                wli.setAddMode(false);
            }
            wishListsRepository.saveAll(wls);
            wishListItemsRepository.saveAll(wlis);
        }
        if (bs == BotStatus.MONTHLY_PAYMENTS_ADD || bs == BotStatus.MONTHLY_PAYMENTS_ADD_NAME
                || bs == BotStatus.MONTHLY_PAYMENTS_ADD_PRICE) {
            List<MonthlyPayments> mps = monthlyPaymentsRepository.findAllByUserId(msg.getFrom().getId());
            for (MonthlyPayments mp : mps) {
                mp.setAddFinish(true);
            }
            monthlyPaymentsRepository.saveAll(mps);
        }
        if (bs == BotStatus.TODO_LIST_ADD || bs == BotStatus.TODO_LIST_ITEMS_ADD) {
            List<ToDoList> tls = toDoListRepository.findAllByUserId(msg.getChatId());
            for (ToDoList tl : tls) {
                tl.setAddMode(false);
            }
            toDoListRepository.saveAll(tls);
        }
    }


    public void startCommandRecieved(Message msg){
        sendMessage(msg, START_TEXT, BotStatus.MAIN, inLineKeyboards.getMain());
    }


    public void sendMessage(Message msg, String textToSend, BotStatus bs, InlineKeyboardMarkup inlineKeyboardMarkup) {

        if (bs != null) {
            setBotStatus(msg, bs);
        }
        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(msg.getChatId()));
        sm.setText(textToSend);

        if(inlineKeyboardMarkup != null) {
            sm.setReplyMarkup(inlineKeyboardMarkup);
        }

        try {
            execute(sm);
            log.info("ANSWER for User " + msg.getFrom().getUserName());
        }
        catch (TelegramApiException e) {
            log.error("send error:" + e);
        }
    }

    public void sendMessageForAll(Message msg) {
        String textToSend = msg.getText().substring(msg.getText().indexOf(" "));
        SendMessage sm = new SendMessage();
        sm.setText(textToSend);
        for (User user : userRepository.findAll()) {
            sm.setChatId(String.valueOf(user.getChatId()));
            try {
                execute(sm);
                log.info("Msg for All");
            }
            catch (TelegramApiException e) {
                log.error("send error:" + e);
            }
        }
    }
}
