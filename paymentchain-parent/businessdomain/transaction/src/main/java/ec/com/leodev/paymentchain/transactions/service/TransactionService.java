package ec.com.leodev.paymentchain.transactions.service;

import ec.com.leodev.paymentchain.transactions.entities.Transaction;
import ec.com.leodev.paymentchain.transactions.repository.ITransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService implements ITransactionService{

    private ITransactionRepository transactionRepository;

    /**
     * Constructor.
     * @param transactionRepository ITransactionRepository
     */
    public TransactionService(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    @Override
    public Transaction create(Transaction input) throws Exception {
        //validations
        if (input.getAmount().equals(0D)) {
            throw new Exception("El monto de la transaccion no puede ser 0.");
        }
        if (input.getDate().toLocalDate().compareTo(LocalDate.now()) <= 0) {
            input.setStatus("LIQUIDADO");
        } else {
            input.setStatus("PENDIENTE");
        }
        return transactionRepository.save(input);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    @Override
    public List<Transaction> findByAccount(String idAccountBan) {
        return transactionRepository.findByIbanAccount(idAccountBan);
    }

    @Override
    public List<Transaction> findByReference(String reference) {
        return transactionRepository.findByReference(reference);
    }
}
