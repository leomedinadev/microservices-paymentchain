package ec.com.leodev.paymentchain.customer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import ec.com.leodev.paymentchain.customer.entitys.Customer;
import ec.com.leodev.paymentchain.customer.entitys.CustomerProduct;
import ec.com.leodev.paymentchain.customer.exception.BusinessRuleException;
import ec.com.leodev.paymentchain.customer.repository.ICustomerRepository;
import ec.com.leodev.paymentchain.customer.service.BusinessTransactionTwoService;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private ICustomerRepository customerRepository;
    
    @Autowired
    private BusinessTransactionTwoService businessTransactionTwoService;

    @Value("${user.role}")
    private String role;

    /**
     * Constructor.
     */
    public CustomerController() {
    }
    
    @GetMapping("/full")
    public Customer get(@RequestParam String code) {
        Customer customer = customerRepository.findByCode(code);
        List<CustomerProduct> products = customer.getProductList();
        products.forEach(item -> {
            String productName = "";
            try {
                productName = businessTransactionTwoService.getNameProduct(item.getProductId());
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            item.setProductName(productName);
        });
        customer.setTransactions(businessTransactionTwoService.getTransactions(customer.getIban()));
        return customer;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Customer>> list() {
        List<Customer> findAll = customerRepository.findAll();
        if (findAll.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(findAll);
        }
    }

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello your role is: " + role;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> get(@PathVariable long id) {
        return customerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable String id, @RequestBody Customer input) {
        return null;
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody Customer input) throws UnknownHostException, BusinessRuleException{
        if (input.getProductList() != null) {
            for (CustomerProduct product : input.getProductList()) {
                String productName = businessTransactionTwoService.getNameProduct(product.getId());
                if (StringUtils.isBlank(productName)) {
                    throw new BusinessRuleException("1025", "Error de validacion, producto no existe", HttpStatus.PRECONDITION_FAILED);
                } else {
                    product.setCustomer(input);
                }
            }
        }
        return new ResponseEntity<>(customerRepository.save(input), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id){
        return null;
    }

}
