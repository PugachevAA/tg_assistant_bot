package puga_tmsk.puga_bot.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
import puga_tmsk.puga_bot.service.keyboards.InLineKeyboards;
import puga_tmsk.puga_bot.service.keyboards.ReplyKeyboards;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShoppingListRepository shoppingListRepository;
    @Autowired
    private UserSettingsRepository userSettingsRepository;


    private static final String HELP_TEXT = "Это мой тестовый бот. \n\n" +
            "Он уже умеет хранить список покупок для удобного похода в магазин :) \n" +
            "Переходи в меню Список покупок, жми Заполнить, вводи по одному в сообщении, а как заполнишь все пункты жми Закончить. \n" +
            "В магазине просто нажимай на пункт, чтобы он ушел из списка. Удачных покупок :)";

    ReplyKeyboards replyKeyboards = new ReplyKeyboards();
    InLineKeyboards inLineKeyboards = new InLineKeyboards();

    public TelegramBot(BotConfig config) {

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

        if (update.hasMessage()) {

            if (update.getMessage().hasText()) {

                messageText = update.getMessage().getText();
                userFirstName = update.getMessage().getChat().getFirstName();
                userName = update.getMessage().getChat().getUserName();
                chatId = update.getMessage().getChatId();
                botStatus = getBotStatus(chatId);

                switch (messageText) {
                    case "/start":
                        setBotStatus(chatId, userName, BotStatus.MAIN);
                        registerUser(update.getMessage());
                        startCommandRecieved(chatId, userFirstName);
                        break;
                    case "/main":
                        setBotStatus(chatId, userName, BotStatus.MAIN);
                        sendMessage(chatId, "Главное меню", "", inLineKeyboards.getMainInLineKeyboard());
                        break;
                    case "/help":
                        sendMessage(chatId, HELP_TEXT, userFirstName, null);
                        break;
                    case "/mydata":
                        User user = userRepository.findById(chatId).get();
                        String userData = user.toString();

                        sendMessage(chatId, userData, userFirstName, null);
                        break;
                    default:
                        if (botStatus == BotStatus.SHOPPING_LIST_ADD) {
                            switch (messageText) {
                                case "/shoplistendadd":
                                    ShoppingListApp.endAdd();
                                    setBotStatus(chatId, userName, BotStatus.SHOPPING_LIST);
                                    sendMessage(chatId, "Твой список покупок:",userFirstName, inLineKeyboards.getShoppingListKeyboard(chatId, messageText, shoppingListRepository));
                                    break;
                                default:
                                    shoppingListAdd(chatId, messageText);
                            }
                        } else {
                            sendMessage(chatId, "Чет не то, бро","", inLineKeyboards.getMainInLineKeyboard());
                            log.info("MESSAGE: User " + userFirstName + " send command " + messageText);


                        }
                }
            }
        } else if (update.hasCallbackQuery()) {

            messageText = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getMessage().getChatId();
            userFirstName = update.getCallbackQuery().getMessage().getChat().getFirstName();
            userName = update.getCallbackQuery().getMessage().getChat().getUserName();
            botStatus = getBotStatus(chatId);

            if (botStatus == BotStatus.SHOPPING_LIST_ADD) {
                switch (messageText) {
                    case "/shoplistendadd":
                        setBotStatus(chatId, userName, BotStatus.SHOPPING_LIST);
                        sendMessage(chatId, "Твой список покупок:", userFirstName, inLineKeyboards.getShoppingListKeyboard(chatId, messageText, shoppingListRepository));
                        break;
                }
                if (messageText.contains("/shoppinglist_")) {
                    String[] itemId = messageText.split("_");
                    shoppingListRepository.deleteById(Long.valueOf(itemId[1]));
                    sendMessage(chatId, "Вводи товары по одному, как закончишь нажми Закончить", userFirstName, inLineKeyboards.getShoppingListAddKeyboard(chatId, messageText, shoppingListRepository));
                }
            } else //if (botStatus == BotStatus.SHOPPING_LIST){
            {
                if (messageText.contains("/shoppinglist_")) {
                    String[] itemId = messageText.split("_");
                    shoppingListRepository.deleteById(Long.valueOf(itemId[1]));
                    sendMessage(chatId, "Твой список покупок:", userFirstName, inLineKeyboards.getShoppingListKeyboard(chatId, messageText, shoppingListRepository));
                }
                    switch (messageText) {
                        case "/shoppinglist":
                            sendMessage(chatId, "Твой список покупок:", userFirstName, inLineKeyboards.getShoppingListKeyboard(chatId, messageText, shoppingListRepository));
                            setBotStatus(chatId, userName, BotStatus.SHOPPING_LIST);
                            break;
                        case "/shoplistadditems":
                            sendMessage(chatId, "Вводи товары по одному, как закончишь нажми Закончить", userFirstName, inLineKeyboards.getShoppingListAddKeyboard(chatId, messageText, shoppingListRepository));
                            setBotStatus(chatId, userName, BotStatus.SHOPPING_LIST_ADD);
                            break;
                        case "/shoplistclear":
                            for (ShoppingList sl : shoppingListRepository.findAll()) {
                                if (sl.getChatId().equals(chatId)) {
                                    shoppingListRepository.deleteById(sl.getId());
                                }
                            }
                            sendMessage(chatId, "Список пуст. Начнем заполнять?)", userFirstName, inLineKeyboards.getShoppingListKeyboard(chatId, messageText, shoppingListRepository));
                            setBotStatus(chatId, userName, BotStatus.SHOPPING_LIST);
                            break;
                    }
                }
        }
    }

    private BotStatus getBotStatus(long chatId) {
        BotStatus botStatus = BotStatus.MAIN;
        UserSettings us;

        if (!userSettingsRepository.findById(chatId).isEmpty()) {
            us = userSettingsRepository.findById(chatId).get();
            for (BotStatus bs : BotStatus.values()) {
                if (bs.getId() == userSettingsRepository.findById(chatId).get().getBotStatus()) {
                    botStatus = bs;
                }
            }
        }

        return botStatus;
    }

    private void setBotStatus(Long chatId, String name, BotStatus bs) {

            UserSettings us = new UserSettings();
            us.setChatId(chatId);
            us.setBotStatus(bs.getId());
            userSettingsRepository.save(us);
            log.info("setBotStatus: " + bs.getId() + " for user: " + name);
    }


    private void shoppingListAdd(Long chatId, String messageText) {
        ShoppingList shoppingList = new ShoppingList();

        shoppingList.setId(shoppingListRepository.count()+1);
        for (long i = 1; i <= shoppingListRepository.count(); i++) {
            if (shoppingListRepository.findById(i).isEmpty()) {
                shoppingList.setId(i);
                break;
            }
        }

        shoppingList.setChatId(chatId);
        shoppingList.setProduct(messageText);
        shoppingListRepository.save(shoppingList);
        sendMessage(chatId, "Вводи товары по одному, как закончишь нажми Закончить", "", inLineKeyboards.getShoppingListAddKeyboard(chatId, messageText, shoppingListRepository));
    }

    private void registerUser(Message message) {
        if(userRepository.findById(message.getChatId()).isEmpty()) {

            Long chatId = message.getChatId();
            Chat chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisterTime(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

            setBotStatus(chatId, user.getUserName(), BotStatus.MAIN);
            log.info("User saved: " + user);
        }
    }

    private void startCommandRecieved(long chatId, String name) throws TelegramApiException{
        String answer = "Привет, " + name + ", Добро пожаловать! Я умею хранить список для похода в магаз :)";

        sendMessage(chatId, answer, name, inLineKeyboards.getMainInLineKeyboard());
        setBotStatus(chatId, name, BotStatus.MAIN);
    }

    private void sendMessage(long chatId, String textToSend, String userName, InlineKeyboardMarkup inlineKeyboardMarkup) {
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
