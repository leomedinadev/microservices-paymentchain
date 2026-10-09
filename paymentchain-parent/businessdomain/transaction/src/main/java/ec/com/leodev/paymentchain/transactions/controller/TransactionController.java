package ec.com.leodev.paymentchain.transactions.controller;

import ec.com.leodev.paymentchain.transactions.entities.Transaction;
import ec.com.leodev.paymentchain.transactions.repository.ITransactionRepository;
import ec.com.leodev.paymentchain.transactions.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private ITransactionService transactionService;

    @GetMapping
    public List<Transaction> list() {
        return transactionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> get(@PathVariable long id) {
        return transactionService.findById(id).map(x -> ResponseEntity.ok(x)).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transactionsByIbaAccount")
    public List<Transaction> getByIbanAccount(@RequestParam String ibanAccount) {
        return transactionService.findByAccount(ibanAccount);
    }

    @GetMapping("/transactionsByReference")
    public List<Transaction> getByReference(@RequestParam String reference) {
        return transactionService.findByReference(reference);
    }

    //TODO: Las transacciones no deberian actualizarse ya que coresponde al a integridad de los datos, pilar fundamental de la seguridad.
//    @PutMapping("/{id}")
//    public ResponseEntity<?> put(@PathVariable String id, @RequestBody Transaction input) {
//        return null;
//    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody Transaction input) throws Exception {
        return ResponseEntity.ok(transactionService.create(input));
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> delete(@PathVariable String id) {
//        return null;
//    }
}
