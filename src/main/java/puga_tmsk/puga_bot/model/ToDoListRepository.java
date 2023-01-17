package puga_tmsk.puga_bot.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ToDoListRepository extends CrudRepository<ToDoList, Long> {

    List<ToDoList> findAllByUserId(long chatId);
    ToDoList findByUserIdAndAddMode(long userId, boolean addMode);
}
