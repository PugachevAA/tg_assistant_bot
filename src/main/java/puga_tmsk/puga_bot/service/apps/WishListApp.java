package puga_tmsk.puga_bot.service.apps;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;
import puga_tmsk.puga_bot.config.BotStatus;
import puga_tmsk.puga_bot.model.WishListItems;
import puga_tmsk.puga_bot.model.WishListItemsRepository;
import puga_tmsk.puga_bot.model.WishLists;
import puga_tmsk.puga_bot.model.WishListsRepository;
import puga_tmsk.puga_bot.service.TelegramBot;

import java.util.List;
import java.util.Locale;

@Slf4j
public class WishListApp {

    TelegramBot telegramBot;

    public WishListApp(TelegramBot tgb) {
        telegramBot = tgb;
    }

    public void addWishList(Message msg) {
        telegramBot.getWishListsRepository().save(new WishLists(msg.getText(), msg.getFrom().getId()));
        telegramBot.sendMessage(msg, "Список вишлистов:", BotStatus.WISH_LISTS,
                telegramBot.getInLineKeyboards().getWishLists(msg.getChatId()));
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
                BotStatus.WISH_LIST_ITEMS, telegramBot.getInLineKeyboards().getWishListMenu(wishListId));

    }

    public void addWishListItemMode(Message msg, long wishListId) {
        changeWishListMode(wishListId, true);
        telegramBot.editMessage(msg, "Введи название своего желания: ",
                BotStatus.WISH_LIST_ITEM_ADD, telegramBot.getInLineKeyboards().getCancelMenu("/wishlist_" + wishListId));
    }

    public void deleteWishListItem(Message msg, String messageText) {
        String[] listId = messageText.split("_");
        long wishListId = Long.parseLong(listId[1]);
        long itemId = Long.parseLong(listId[3]);
        telegramBot.getWishListItemsRepository().deleteById(itemId);
        telegramBot.editMessage(msg, telegramBot.getWishListsRepository().findById(wishListId).get().getTitle(),
                BotStatus.WISH_LIST_ITEMS, telegramBot.getInLineKeyboards().getWishListMenu(wishListId));
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
        if (msg.getText().toLowerCase().contains("http://") || msg.getText().toLowerCase().contains("https://")) {
            String[] linkArr = msg.getText().split("http");
            for (String l : linkArr) {
                if (l.toLowerCase().contains("://")) {
                    linkArr = l.split(" ");
                    log.info("http" + linkArr[0]);
                    wli.setLink("http" + linkArr[0]);
                    break;
                }
            }
            wli.setAddMode(false);
            telegramBot.getWishListItemsRepository().save(wli);
            changeWishListMode(wishListId, false);
            telegramBot.sendMessage(msg, telegramBot.getWishListItemsRepository().findById(wli.getId()).get().getTitle(),
                    BotStatus.WISH_LIST_ITEMS, telegramBot.getInLineKeyboards().getWishListItemMenu(wishListId, wli.getId()));
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
        telegramBot.getMenu().wishListItemMenu(msg, wishListId, itemId);
    }

    public void deleteWishList(Message msg, String messageText) {
        String[] listId = messageText.split("_");
        long wishListId = Long.parseLong(listId[1]);
        telegramBot.getWishListsRepository().deleteById(wishListId);
        telegramBot.editMessage(msg, "Список вишлистов",
                BotStatus.WISH_LISTS, telegramBot.getInLineKeyboards().getWishLists(msg.getChatId()));
        List<WishListItems> wlis = telegramBot.getWishListItemsRepository().findAllByWishListId(wishListId);
        telegramBot.getWishListItemsRepository().deleteAll(wlis);

    }

    public void copyWishList(Message msg, String messageText) {
        String[] listId = messageText.split("_");
        long wishListId = Long.parseLong(listId[1]);
        List<WishListItems> wli = telegramBot.getWishListItemsRepository().findAllByWishListId(wishListId);
        String answer = "Вот мой вишлист:\n\n";
        int n = 1;
        for (WishListItems item : wli) {
            answer = answer + n + ". " + item.getTitle() + "\n";
            if (!item.getLink().equals("")) {
                answer = answer + "Ссылка: " + item.getLink() + "\n";
            }
            answer = answer + "\n";
            n = n + 1;
        }
        telegramBot.sendMessage(msg, answer, null, null);
    }
}

