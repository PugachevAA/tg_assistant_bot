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
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import puga_tmsk.puga_bot.config.BotConfig;
import puga_tmsk.puga_bot.config.BotStatus;
import puga_tmsk.puga_bot.model.*;
import puga_tmsk.puga_bot.service.apps.ShoppingListApp;
import puga_tmsk.puga_bot.service.apps.UserActions;
import puga_tmsk.puga_bot.service.keyboards.InLineKeyboards;
import puga_tmsk.puga_bot.service.keyboards.ReplyKeyboards;
import puga_tmsk.puga_bot.service.updateHandlers.MainTextHandler;

import java.util.*;

@Slf4j
@Component
@Getter
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    private final UserActions userActions;
    private final ShoppingListApp shoppingList;
    private final ReplyKeyboards replyKeyboards;
    private final InLineKeyboards inLineKeyboards;
    private final MainTextHandler mainTextHandler;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShoppingListRepository shoppingListRepository;
    @Autowired
    private UserSettingsRepository userSettingsRepository;


    public TelegramBot(BotConfig config) {

        userActions = new UserActions(this);
        shoppingList = new ShoppingListApp(this);
        replyKeyboards = new ReplyKeyboards();
        inLineKeyboards = new InLineKeyboards();
        mainTextHandler = new MainTextHandler(this, userActions, shoppingList);

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

        String messageText;
        String userFirstName;
        String userName;
        long chatId;
        BotStatus botStatus;
        Message msg;

        if (update.hasMessage()) {

            mainTextHandler.mesageHandler(update);

        } else if (update.hasCallbackQuery()) {

            msg = update.getCallbackQuery().getMessage();

            messageText = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getMessage().getChatId();
            userFirstName = update.getCallbackQuery().getMessage().getChat().getFirstName();
            userName = update.getCallbackQuery().getMessage().getChat().getUserName();
            botStatus = getBotStatus(chatId);

            if (botStatus == BotStatus.SHOPPING_LIST_ADD) {
                switch (messageText) {
                    case "/shoplistendadd":
                        setBotStatus(chatId, userName, BotStatus.SHOPPING_LIST);
                        sendMessage(chatId, "Твой список покупок:", userFirstName, inLineKeyboards.getShoppingList(chatId, messageText, shoppingListRepository));
                        break;
                }
                if (messageText.contains("/shoppinglist_")) {
                    String[] itemId = messageText.split("_");
                    shoppingListRepository.deleteById(Long.valueOf(itemId[1]));

                    sendMessage(chatId, "Вводи товары по одному, как закончишь нажми Закончить", userFirstName, inLineKeyboards.getShoppingListAdd(chatId, messageText, shoppingListRepository));

                }
            } else //if (botStatus == BotStatus.SHOPPING_LIST){
            {
                if (messageText.contains("/shoppinglist_")) {
                    String[] itemId = messageText.split("_");
                    shoppingListRepository.deleteById(Long.valueOf(itemId[1]));
                    editMessage(chatId, msg, "", inLineKeyboards.getShoppingList(chatId, messageText, shoppingListRepository));
                }
                    switch (messageText) {
                        case "/main":
                            setBotStatus(chatId, userName, BotStatus.MAIN);
                            editMessage(chatId, msg, "Главное меню", inLineKeyboards.getMain());
                            break;
                        case "/lists":
                            setBotStatus(chatId, userName, BotStatus.MAIN);
                            editMessage(chatId, msg, "Списки", inLineKeyboards.getLists());
                            break;
                        case "/shoppinglist":
                            setBotStatus(chatId, userName, BotStatus.SHOPPING_LIST);
                            editMessage(chatId, msg, "Сходить в магазин", inLineKeyboards.getShoppingList(chatId, messageText, shoppingListRepository));
                            break;
                        case "/wishlist":
                            //sendMessage(chatId, "Твой вишлист:", userFirstName, inLineKeyboards.g(chatId, messageText, shoppingListRepository));
                            //setBotStatus(chatId, userName, BotStatus.SHOPPING_LIST);
                            break;
                        case "/shoplistadditems":
                            setBotStatus(chatId, userName, BotStatus.SHOPPING_LIST_ADD);
                            editMessage(chatId, msg, "Вводи и отправляй покупки по одному сообщению, в конце нажми Закончить", inLineKeyboards.getShoppingListAdd(chatId, messageText, shoppingListRepository));
                            break;
//                        case "/shoplistclear":
//                            setBotStatus(chatId, userName, BotStatus.SHOPPING_LIST);
//                            shoppingList.clear(chatId);
//                            editMessage(chatId, msg, inLineKeyboards.getShoppingList(chatId, messageText, shoppingListRepository));
//                            break;
                    }
                }
        }
    }

    public void editMessage(long chatId, Message msg, String newText, InlineKeyboardMarkup mainInLineKeyboard) {

        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setReplyMarkup(mainInLineKeyboard);
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(msg.getMessageId());

        if (!newText.equals("")) {
            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(String.valueOf(chatId));
            editMessageText.setMessageId(msg.getMessageId());
            editMessageText.setText(newText);
            try {
                execute(editMessageText);
                log.info("[" + userSettingsRepository.findById(chatId).get().getBotStatus() + "] Edit message");
            }
            catch (TelegramApiException e) {
                log.error("edit error:" + e);
            }
        }

        try {
            execute(editMessageReplyMarkup);
            log.info("[" + userSettingsRepository.findById(chatId).get().getBotStatus() + "] Edit message");
        }
        catch (TelegramApiException e) {
            log.error("edit error:" + e);
        }
    }

    public BotStatus getBotStatus(long chatId) {
        BotStatus botStatus = BotStatus.MAIN;

        if (!userSettingsRepository.findById(chatId).isEmpty()) {
            for (BotStatus bs : BotStatus.values()) {
                if (bs.getId() == userSettingsRepository.findById(chatId).get().getBotStatus()) {
                    botStatus = bs;
                }
            }
        }
        return botStatus;
    }


    public void setBotStatus(Long chatId, String name, BotStatus bs) {

            UserSettings us = new UserSettings();
            us.setChatId(chatId);
            us.setBotStatus(bs.getId());
            userSettingsRepository.save(us);
            log.info("setBotStatus: " + bs.getId() + " for user: " + name);
    }


    public void startCommandRecieved(long chatId, String name){
        String answer = "Привет, " + name + ", Добро пожаловать! Я умею хранить список для похода в магаз :)";

        sendMessage(chatId, answer, name, inLineKeyboards.getMain());
        setBotStatus(chatId, name, BotStatus.MAIN);
    }


    public void sendMessage(long chatId, String textToSend, String userName, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);


        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
            log.info("[" + userSettingsRepository.findById(chatId).get().getBotStatus() + "] ANSWER: User " + userName + ", text: " + textToSend);
        }
        catch (TelegramApiException e) {
            log.error("send error:" + e);
        }
    }
}
