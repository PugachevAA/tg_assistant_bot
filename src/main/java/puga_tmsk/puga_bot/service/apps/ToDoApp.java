package puga_tmsk.puga_bot.service.apps;

import org.telegram.telegrambots.meta.api.objects.Message;
import puga_tmsk.puga_bot.config.BotStatus;
import puga_tmsk.puga_bot.model.ShoppingList;
import puga_tmsk.puga_bot.model.ToDoList;
import puga_tmsk.puga_bot.model.ToDoListItem;
import puga_tmsk.puga_bot.model.WishLists;
import puga_tmsk.puga_bot.service.TelegramBot;

public class ToDoApp {
    TelegramBot telegramBot;

    public ToDoApp(TelegramBot tgb) {
        this.telegramBot = tgb;
    }

    public void addToDoList(Message msg) {
        telegramBot.getToDoListRepository().save(new ToDoList(msg.getText(), msg.getChatId()));
        telegramBot.getMenu().newToDoLists(msg);
    }

    public void openToDoList(Message msg, String messageText) {
        String[] itemId = messageText.split("_");
        long id = Long.parseLong(itemId[2]);
        telegramBot.getMenu().toDoListItemsMenu(msg, telegramBot.getToDoListRepository().findById(id).get().getTitle(), id);
    }

    public void deleteTodoList(Message msg, String messageText) {
        String[] itemId = messageText.split("_");
        long id = Long.parseLong(itemId[2]);
        telegramBot.getToDoListRepository().deleteById(id);
        telegramBot.getMenu().toDoLists(msg);
    }

    public void addTodoListItemMode(Message msg, long todoListId) {
        changeTodoListMode(todoListId, true);
        telegramBot.getMenu().toDoListItemsAddMenu(msg);
    }

    public void changeTodoListMode(long todoListId, boolean b) {
        ToDoList tl = telegramBot.getToDoListRepository().findById(todoListId).get();
        tl.setAddMode(b);
        telegramBot.getToDoListRepository().save(tl);
    }

    public void addToDoListItems(Message msg) {
        ToDoList tl = telegramBot.getToDoListRepository().findByUserIdAndAddMode(msg.getChatId(), true);
        ToDoListItem tli = new ToDoListItem();
        tli.setTitle(msg.getText());
        tli.setTodoListId(tl.getId());
        telegramBot.getToDoListItemRepository().save(tli);
        telegramBot.getMenu().newToDoListItemsMenu(msg, tl.getTitle(), tl.getId());
    }

    public void stopTodoListItemsAdd(Message msg, long todoListId) {
        changeTodoListMode(todoListId, false);
        telegramBot.getMenu().toDoListItemsMenu(msg, telegramBot.getToDoListRepository().findById(todoListId).get().getTitle(), todoListId);
    }

    public void deleteTodoListItem(Message msg, long todoListId, long itemId) {
        telegramBot.getToDoListItemRepository().deleteById(itemId);
        telegramBot.getMenu().toDoListItemsMenu(msg, telegramBot.getToDoListRepository().findById(todoListId).get().getTitle(), todoListId);
    }
}
