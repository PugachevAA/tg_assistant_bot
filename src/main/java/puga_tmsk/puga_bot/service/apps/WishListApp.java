package puga_tmsk.puga_bot.service.apps;

import org.telegram.telegrambots.meta.api.objects.Message;
import puga_tmsk.puga_bot.config.BotStatus;
import puga_tmsk.puga_bot.model.WishListItems;
import puga_tmsk.puga_bot.model.WishListItemsRepository;
import puga_tmsk.puga_bot.model.WishLists;
import puga_tmsk.puga_bot.model.WishListsRepository;
import puga_tmsk.puga_bot.service.TelegramBot;

public class WishListApp {

    TelegramBot telegramBot;

    public WishListApp(TelegramBot tgb) {
        telegramBot = tgb;
    }

    public void addWishList(Message msg) {
        telegramBot.getWishListsRepository().save(new WishLists(msg.getText(), msg.getFrom().getId()));
        telegramBot.sendMessage(msg, "Список вишлистов:", BotStatus.WISH_LISTS,
                telegramBot.getInLineKeyboards().getWishLists(msg.getChatId(), telegramBot.getWishListsRepository()));
    }

    public void addWishListItem(Message msg) {
        long wishListId = telegramBot.getWishListsRepository().findByUserIdAndAddMode(msg.getFrom().getId(), true).getId();
        WishListItems item = new WishListItems();
        item.setWishListId(wishListId);
        item.setLink("");
        item.setTitle(msg.getText());
        telegramBot.getWishListItemsRepository().save(item);
        WishLists wl = telegramBot.getWishListsRepository().findById(wishListId).get();
        wl.setAddMode(false);
        telegramBot.getWishListsRepository().save(wl);
        telegramBot.sendMessage(msg, "Вишлист: " + telegramBot.getWishListsRepository().findById(wishListId).get().getTitle(),
                BotStatus.WISH_LIST_ITEMS, telegramBot.getInLineKeyboards().getWishListMenu(wishListId, telegramBot.getWishListItemsRepository()));

    }

    public void addWishListItemMode(Message msg, long wishListId) {
        changeWishListItemMode(wishListId, true);
        telegramBot.editMessage(msg, "Введи название своего желания: ",
                BotStatus.WISH_LIST_ITEM_ADD, telegramBot.getInLineKeyboards().getCancelMenu("/wishlist_" + wishListId));
    }

    public void deleteWishListItem(Message msg, String messageText) {
        String[] listId = messageText.split("_");
        long wishListId = Long.parseLong(listId[1]);
        long itemId = Long.parseLong(listId[3]);
        telegramBot.getWishListItemsRepository().deleteById(itemId);
        telegramBot.editMessage(msg, telegramBot.getWishListItemsRepository().findById(itemId).get().getTitle(),
                BotStatus.WISH_LIST_ITEMS, telegramBot.getInLineKeyboards().getWishListItemMenu(wishListId, itemId, telegramBot.getWishListItemsRepository()));
    }

    public void addWishListItemLinkMode(Message msg, String messageText) {
        String[] listId = messageText.split("_");
        long wishListId = Long.parseLong(listId[1]);
        long itemId = Long.parseLong(listId[3]);
        changeWishListItemMode(itemId, true);
        changeWishListMode(wishListId, true);
        telegramBot.editMessage(msg, "Вставь ссылку для " + telegramBot.getWishListItemsRepository().findById(itemId).get().getTitle() + ": ",
                BotStatus.WISH_LIST_ITEM_ADD_LINK, telegramBot.getInLineKeyboards().getCancelMenu("/wishlist_" + wishListId + "_item_" + itemId + "_cancel"));
    }

    public void addWishListItemLink(Message msg) {
        long wishListId = telegramBot.getWishListsRepository().findByUserIdAndAddMode(msg.getFrom().getId(), true).getId();
        WishListItems wli = telegramBot.getWishListItemsRepository().findByWishListIdAndAddMode(wishListId, true);
        if (msg.getText().contains("http://") || msg.getText().contains("https://")) {
            wli.setLink(msg.getText());
            wli.setAddMode(false);
            telegramBot.getWishListItemsRepository().save(wli);
            changeWishListMode(wishListId, false);
            telegramBot.sendMessage(msg, telegramBot.getWishListItemsRepository().findById(wli.getId()).get().getTitle(),
                    BotStatus.WISH_LIST_ITEMS, telegramBot.getInLineKeyboards().getWishListItemMenu(wishListId, wli.getId(), telegramBot.getWishListItemsRepository()));
        } else {
            telegramBot.sendMessage(msg, "Некорректная ссылка, попробуй еще раз:",
                    BotStatus.WISH_LIST_ITEM_ADD_LINK, telegramBot.getInLineKeyboards().getCancelMenu("/wishlist_" + wishListId + "_item_" + wli.getId() + "_cancel"));
        }
    }

    public void changeWishListItemMode(long id, boolean status) {
        WishListItems wli = telegramBot.getWishListItemsRepository().findById(id).get();
        wli.setAddMode(status);
        telegramBot.getWishListItemsRepository().save(wli);
    }

    public void changeWishListMode(long wishListId, boolean b) {
        WishLists wl = telegramBot.getWishListsRepository().findById(wishListId).get();
        wl.setAddMode(b);
        telegramBot.getWishListsRepository().save(wl);
    }

    public void cancelAddWishListItemLink(Message msg, String messageText) {
        String[] listId = messageText.split("_");
        long wishListId = Long.parseLong(listId[1]);
        long itemId = Long.parseLong(listId[3]);
        changeWishListMode(wishListId, false);
        changeWishListItemMode(itemId, false);
        telegramBot.editMessage(msg, telegramBot.getWishListItemsRepository().findById(itemId).get().getTitle(),
                BotStatus.WISH_LIST_ITEMS, telegramBot.getInLineKeyboards().getWishListItemMenu(wishListId, itemId, telegramBot.getWishListItemsRepository()));
    }
}

