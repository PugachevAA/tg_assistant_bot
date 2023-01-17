package puga_tmsk.puga_bot.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import puga_tmsk.puga_bot.config.BotStatus;

public class Menu {

    TelegramBot telegramBot;
    private final String MAIN_TITLE = "Главное меню:";
    private final String LISTS_TITLE = "Списки:";
    private final String SHOPPING_LIST_TITLE = "Сходить в магазин:";
    private final String SHOPPING_LIST_ADD_TITLE = "Вводи и отправляй покупки по одному сообщению, в конце нажми Закончить:";
    private final String MONTHLY_PAYMENTS_TITLE = "Главное меню:";
    private final String MONTHLY_PAYMENTS_ADD_TITLE = "Введи название платежа:";
    private final String WISHLISTS_TITLE = "Список вишлистов:";
    private final String WISHLIST_ADD_TITLE = "Введи название списка:";
    private final String TODO_TITLE = "Списки TODO:";
    private final String TODO_LIST_ADD_TITLE = "Введи название списка TODO:";
    private final String TODO_LIST_TITLE = "Список TODO - ";
    private final String TODO_LIST_ITEM_ADD_TITLE = "Вводи и отправляй дела по одному сообщению, в конце нажми Закончить:";

    public Menu(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void main(Message msg) {
        telegramBot.sendMessage(msg, MAIN_TITLE, BotStatus.MAIN, telegramBot.getInLineKeyboards().getMain());
    }

        public void listsMenu(Message msg) {
            telegramBot.editMessage(msg, LISTS_TITLE, BotStatus.MAIN, telegramBot.getInLineKeyboards().getLists());
        }

            public void shoppingListMenu(Message msg, BotStatus bs) {
                telegramBot.editMessage(msg, SHOPPING_LIST_TITLE, bs,
                        telegramBot.getInLineKeyboards().getShoppingList(msg.getChatId()));
            }

                public void shoppingListAddMenu(Message msg) {
                    telegramBot.editMessage(msg, SHOPPING_LIST_ADD_TITLE,
                            BotStatus.SHOPPING_LIST_ADD, telegramBot.getInLineKeyboards().getShoppingListAdd(msg.getChatId()));
                }

            public void wishListsMenu(Message msg) {
                telegramBot.editMessage(msg, WISHLISTS_TITLE,
                        BotStatus.WISH_LISTS, telegramBot.getInLineKeyboards().getWishLists(msg.getChatId()));
            }

                public void wishListAddMenu(Message msg) {
                    telegramBot.editMessage(msg, WISHLIST_ADD_TITLE,
                            BotStatus.WISH_LIST_ADD, telegramBot.getInLineKeyboards().getCancelMenu("/wishlists"));
                }

                public void wishListItemMenu(Message msg,long wishListId, long itemId){
                    telegramBot.editMessage(msg, telegramBot.getWishListItemsRepository().findById(itemId).get().getTitle(),
                            BotStatus.WISH_LIST_ITEMS, telegramBot.getInLineKeyboards().getWishListItemMenu(wishListId, itemId));
                }

            public void toDoLists(Message msg) {
                telegramBot.editMessage(msg, TODO_TITLE, BotStatus.TODO,
                        telegramBot.getInLineKeyboards().getToDoListsMenu(msg.getChatId()));
            }
            public void newToDoLists(Message msg) {
                telegramBot.sendMessage(msg, TODO_TITLE, BotStatus.TODO,
                        telegramBot.getInLineKeyboards().getToDoListsMenu(msg.getChatId()));
            }

        public void monthlyPaymentsMenu(Message msg) {
            telegramBot.editMessage(msg, MONTHLY_PAYMENTS_TITLE, BotStatus.MONTHLY_PAYMENTS,
                    telegramBot.getInLineKeyboards().getMonthlyPayments(msg.getChatId()));
        }

            public void monthlyPaymentsAddMenu(Message msg) {
                telegramBot.editMessage(msg, MONTHLY_PAYMENTS_ADD_TITLE, BotStatus.MONTHLY_PAYMENTS_ADD_NAME,
                        telegramBot.getInLineKeyboards().getMonthlyPaymentsAdd());
            }


    public void toDoListAddMenu(Message msg) {
        telegramBot.editMessage(msg, TODO_LIST_ADD_TITLE, BotStatus.TODO_LIST_ADD,
                telegramBot.getInLineKeyboards().getCancelMenu("/todo_lists_menu"));
    }

    public void toDoListItemsMenu(Message msg, String listName, long listId) {
        telegramBot.editMessage(msg, TODO_LIST_TITLE + listName, BotStatus.TODO_LIST_ITEMS,
                telegramBot.getInLineKeyboards().getToDoListItemsMenu(listId));
    }

    public void toDoListItemsAddMenu(Message msg, long todoListId) {
        telegramBot.editMessage(msg, TODO_LIST_ITEM_ADD_TITLE,
                BotStatus.TODO_LIST_ITEMS_ADD, telegramBot.getInLineKeyboards().getTodoListItemsAdd(todoListId));
    }

    public void newToDoListItemsMenu(Message msg, String title, long listId) {
        telegramBot.sendMessage(msg, TODO_LIST_TITLE + title, BotStatus.TODO_LIST_ITEMS_ADD,
                telegramBot.getInLineKeyboards().getTodoListItemsAdd(listId));}
}
