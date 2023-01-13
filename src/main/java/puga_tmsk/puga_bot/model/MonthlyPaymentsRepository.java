package puga_tmsk.puga_bot.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MonthlyPaymentsRepository extends CrudRepository<MonthlyPayments, Long> {

    MonthlyPayments findByUserIdAndAddFinish(long userId, boolean addOk);
    List<MonthlyPayments> findAllByUserId(long userId);
}
