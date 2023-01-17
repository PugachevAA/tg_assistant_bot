package puga_tmsk.puga_bot.model;

import org.springframework.data.repository.CrudRepository;

public interface ToDoListItemRepository  extends CrudRepository<ToDoListItem, Long> {
    ToDoListItem findByTodoListId(long todoId);

    Iterable<? extends ToDoListItem> findAllByTodoListId(long todoId);
}
