package ec.com.leodev.paymentchain.transactions.repository;

import ec.com.leodev.paymentchain.transactions.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ITransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t where t.ibanAccount = ?1")
    List<Transaction> findByIbanAccount(String ibanAccount);

    @Query("select t from Transaction t where t.reference = ?1")
    List<Transaction> findByReference(String reference);
}
