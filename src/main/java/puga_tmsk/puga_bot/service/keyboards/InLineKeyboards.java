package puga_tmsk.puga_bot.service.keyboards;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import puga_tmsk.puga_bot.model.*;
import puga_tmsk.puga_bot.service.TelegramBot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InLineKeyboards {
    private final String BTN_DELETE = EmojiParser.parseToUnicode(":wastebasket: Удалить");
    private final String BTN_DELETE_EMJ = EmojiParser.parseToUnicode(":wastebasket:");
    private final String BTN_ADD = EmojiParser.parseToUnicode(":heavy_plus_sign: Добавить");
    private final String BTN_ADD_EMJ = EmojiParser.parseToUnicode(":heavy_plus_sign:");
    private final String BTN_BACK = EmojiParser.parseToUnicode(":leftwards_arrow_with_hook: Назад");
    private final String BTN_CANCEL = EmojiParser.parseToUnicode(":heavy_multiplication_x: Отменить");
    private final String BTN_WRITE = EmojiParser.parseToUnicode(":memo: Заполнить");
    private final String BTN_END = EmojiParser.parseToUnicode(":white_check_mark: Закончить");
    private final String BTN_COPY = EmojiParser.parseToUnicode(":outbox_tray: Скопир-ть");
    private final String BTN_COPY_EMJ = EmojiParser.parseToUnicode(":outbox_tray:");

    TelegramBot telegramBot;
    public InLineKeyboards(TelegramBot tgb) {
        telegramBot = tgb;
    }

    public InlineKeyboardMarkup getMain() {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.add(row1btn(button("Списки", "/lists")));
        keyboardRows.add(row1btn(button("Ежемесячные платежи","/monthly_payments")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    public InlineKeyboardMarkup getLists() {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.add(row1btn(button("Сходить в магазин","/shoppinglist")));
        keyboardRows.add(row1btn(button("Твои вишлисты", "/wishlists_menu")));
        keyboardRows.add(row1btn(button("TODO - списки дел", "/todo_lists_menu")));
        //keyboardRows.add(row1btn(button("Подписки (в разработке)","/subscriptions")));
        keyboardRows.add(row1btn(button(BTN_BACK, "/main")));

        return new InlineKeyboardMarkup(keyboardRows);
    }


    public InlineKeyboardMarkup getShoppingList(Long chatId) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.addAll(generateShoppingListButtons(chatId));
        keyboardRows.add(row2btns(button(BTN_WRITE, "/shoplistadditems"), button(BTN_BACK, "/lists")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    public InlineKeyboardMarkup getShoppingListAdd(Long chatId) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.addAll(generateShoppingListButtons(chatId));
        keyboardRows.add(row1btn(button(BTN_END, "/shoplistendadd")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    public InlineKeyboardMarkup getMonthlyPayments(Long chatId) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.addAll(generateMonthlyPaymentsButtons(chatId));
        keyboardRows.add(row2btns(button(BTN_ADD, "/monthly_payments_add"), button(BTN_BACK, "/main")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    public InlineKeyboardMarkup getMonthlyPaymentsAdd() {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.add(row1btn(button(BTN_CANCEL, "/monthly_payments_add_cancel")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    public InlineKeyboardMarkup getMonthlyPaymentsEdit(long id) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        //keyboardRows.add(row2btns(button("Изменить название", "/monthly_payments_item_edittitle_" + id),
        //                            button("Изменить платеж", "/monthly_payments_item_editprice_" + id)));
        keyboardRows.add(row1btn(button(BTN_DELETE,"/monthly_payments_item_delete_" + id)));
        keyboardRows.add(row1btn(button(BTN_BACK, "/monthly_payments")));

        return new InlineKeyboardMarkup(keyboardRows);
    }




    public InlineKeyboardMarkup getWishLists(Long chatId) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.addAll(generateWishListsButtons(chatId));
        keyboardRows.add(row2btns(button(BTN_ADD, "/wishlists_add"), button(BTN_BACK, "/lists")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    public InlineKeyboardMarkup getWishListMenu(long wishListId) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.addAll(generateWishListItemsButtons(wishListId));
        keyboardRows.add(row3btns(button(BTN_ADD_EMJ, "/wishlist_" + wishListId + "_items_add"),
                button(BTN_COPY_EMJ, "/wishlist_" + wishListId + "_copy"),
                button(BTN_DELETE_EMJ, "/wishlist_" + wishListId + "_delete")));
        keyboardRows.add(row1btn(button(BTN_BACK, "/wishlists_menu")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    private List<List<InlineKeyboardButton>> generateWishListItemsButtons(long wishlistId) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        if (telegramBot.getWishListItemsRepository().count() > 0) {
            for (WishListItems wli : telegramBot.getWishListItemsRepository().findAllByWishListId(wishlistId)) {
                keyboardRows.add(row1btn(button(wli.getTitle(), "/wishlist_" + wishlistId + "_item_" + wli.getId())));
            }

        }
        return keyboardRows;
    }

    private Collection<? extends List<InlineKeyboardButton>> generateWishListsButtons(Long chatId) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        if (telegramBot.getWishListsRepository().count() > 0) {
            for (WishLists wl : telegramBot.getWishListsRepository().findAllByUserId(chatId)) {
                keyboardRows.add(row1btn(button(wl.getTitle(), "/wishlist_" + wl.getId())));
            }

        }
        return keyboardRows;
    }

    public InlineKeyboardMarkup getCancelMenu(String backData) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.add(row1btn(button(BTN_CANCEL, backData)));

        return new InlineKeyboardMarkup(keyboardRows);
    }


    private Collection<? extends List<InlineKeyboardButton>> generateMonthlyPaymentsButtons(Long userId) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        if (telegramBot.getMonthlyPaymentsRepository().count() > 0) {
            for (MonthlyPayments mp : telegramBot.getMonthlyPaymentsRepository().findAll()) {
                if (mp.getUserId() == userId) {
//                    keyboardRows.add(row2btns(button(mp.getTitle() + ", " + mp.getPrice(), "/monthly_payments_" + mp.getId()),
//                                                button(mp.getTitle(), "/monthly_payments_" + mp.getId() + "_payed")));
                    keyboardRows.add(row1btn(button(mp.getTitle() + ", " + mp.getPrice() + "руб.", "/monthly_payments_item_view_" + mp.getId())));
                }
            }
        }
        return keyboardRows;
    }


    private List<List<InlineKeyboardButton>> generateShoppingListButtons(Long chatId) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        if (telegramBot.getShoppingListRepository().count() > 0) {
            for (ShoppingList shoppingList1 : telegramBot.getShoppingListRepository().findAll()) {
                if (shoppingList1.getChatId().equals(chatId)) {
                    keyboardRows.add(row1btn(button(shoppingList1.getProduct(), "/shoppinglist_" + shoppingList1.getId())));
                }
            }
        }
        return keyboardRows;
    }


    private List<InlineKeyboardButton> row1btn(InlineKeyboardButton btn) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(btn);
        return row;
    }
    private List<InlineKeyboardButton> row2btns(InlineKeyboardButton btn1,InlineKeyboardButton btn2) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(btn1);
        row.add(btn2);
        return row;
    }
    private List<InlineKeyboardButton> row3btns(InlineKeyboardButton btn1,InlineKeyboardButton btn2,InlineKeyboardButton btn3) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(btn1);
        row.add(btn2);
        row.add(btn3);
        return row;
    }

    private InlineKeyboardButton button(String text, String data) {
        InlineKeyboardButton checkButton = new InlineKeyboardButton();
        checkButton.setText(text);
        checkButton.setCallbackData(data);
        return checkButton;
    }

    private InlineKeyboardButton linkButton(String text, String data) {
        InlineKeyboardButton checkButton = new InlineKeyboardButton();
        checkButton.setText(text);
        checkButton.setUrl(data);
        return checkButton;
    }

    public InlineKeyboardMarkup getWishListItemMenu(long wishListId, long itemId) {


        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        //keyboardRows.add(row2btns(button("Изменить название", "/monthly_payments_item_edittitle_" + id),
        //                            button("Изменить платеж", "/monthly_payments_item_editprice_" + id)));
        WishListItems wli = telegramBot.getWishListItemsRepository().findById(itemId).get();
        if (wli.getLink().equals("")) {
            keyboardRows.add(row1btn(button("Добавить ссылку", "/wishlist_" + wishListId + "_item_" + itemId + "_addlink")));
        } else {
            keyboardRows.add(row1btn(linkButton("Открыть ссылку",wli.getLink())));
        }
        keyboardRows.add(row1btn(button(BTN_DELETE,"/wishlist_" + wishListId + "_item_" + itemId + "_delete")));
        keyboardRows.add(row1btn(button(BTN_BACK, "/wishlist_" + wishListId)));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    public InlineKeyboardMarkup getToDoListsMenu(Long chatId) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.addAll(generateToDoListsButtons(chatId));
        keyboardRows.add(row2btns(button(BTN_ADD, "/todo_list_add"), button(BTN_BACK, "/lists")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    private Collection<? extends List<InlineKeyboardButton>> generateToDoListsButtons(Long chatId) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        if (telegramBot.getToDoListRepository().count() > 0) {
            for (ToDoList tdl : telegramBot.getToDoListRepository().findAllByUserId(chatId)) {
                keyboardRows.add(row1btn(button(tdl.getTitle(), "/todo_list_" + tdl.getId())));
            }

        }
        return keyboardRows;
    }

    public InlineKeyboardMarkup getToDoListItemsMenu(long todoId) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.addAll(generateToDoListItemsButtons(todoId));
        keyboardRows.add(row2btns(button(BTN_ADD, "/todo_list_" + todoId + "_items_add"), button(BTN_DELETE, "/todo_list_" + todoId + "_delete")));
        keyboardRows.add(row1btn(button(BTN_BACK, "/todo_lists_menu")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    private Collection<? extends List<InlineKeyboardButton>> generateToDoListItemsButtons(long todoId) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        if (telegramBot.getToDoListRepository().count() > 0) {
            for (ToDoListItem wli : telegramBot.getToDoListItemRepository().findAllByTodoListId(todoId)) {
                keyboardRows.add(row1btn(button(wli.getTitle(), "/todo_list_" + todoId + "_item_" + wli.getId())));
            }

        }
        return keyboardRows;
    }


    public InlineKeyboardMarkup getTodoListItemsAdd(long todoId) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.addAll(generateToDoListItemsButtons(todoId));
        keyboardRows.add(row1btn(button(BTN_END, "/todo_list_" + todoId + "_endadd")));

        return new InlineKeyboardMarkup(keyboardRows);
    }
}
