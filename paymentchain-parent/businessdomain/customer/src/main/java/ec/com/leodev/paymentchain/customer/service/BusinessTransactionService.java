package ec.com.leodev.paymentchain.customer.service;

import com.fasterxml.jackson.databind.JsonNode;
import ec.com.leodev.paymentchain.customer.entitys.Customer;
import ec.com.leodev.paymentchain.customer.entitys.CustomerProduct;
import ec.com.leodev.paymentchain.customer.exception.BusinessRuleException;
import ec.com.leodev.paymentchain.customer.repository.ICustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class BusinessTransactionService {

    @Autowired
    ICustomerRepository customerRepository;

    private final WebClient.Builder webClientBuilder;

    public BusinessTransactionService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    //define timeout
    TcpClient tcpClient = TcpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

    public Customer get(String code) {
        Customer customer = customerRepository.findByCode(code);
        if (customer.getProductList() != null) {
            List<CustomerProduct> products = customer.getProductList();
            products.forEach(dto -> {
                try {
                    String productName = getProductName(dto.getProductId());
                    dto.setProductName(productName);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(BusinessTransactionService.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
        customer.setTransactions(getTransactions(customer.getIban()));
        return customer;
    }

    public Customer save(Customer input) throws BusinessRuleException, UnknownHostException {
        if (input.getProductList() != null) {
            for (CustomerProduct dto : input.getProductList()) {
                String productName = getProductName(dto.getProductId());
                if (StringUtils.isBlank(productName)) {
                    throw new BusinessRuleException("1025", "Error de validacion, producto no existe", HttpStatus.PRECONDITION_FAILED);
                } else {
                    dto.setCustomer(input);
                }
            }
        }
        return customerRepository.save(input);
    }

    private <T> List<T> getTransactions(String accountIban) {
        List<T> trasnsactions = new ArrayList<>();
        try {
            WebClient client = webClientBuilder.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                    .baseUrl("http://businessdomain-transactions/paymentTransaction")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultUriVariables(Collections.singletonMap("url", "http://businessdomain-transactions/paymentTransaction"))
                    .build();
            List<Object> block = client.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder
                            .path("/transaction/transactionsByIbaAccount")
                            .queryParam("ibanAccount", accountIban)
                            .build())
                    .retrieve().bodyToFlux(Object.class).collectList().block();
            trasnsactions = (List<T>) block;
        } catch (Exception e) {
            return trasnsactions;
        }
        return trasnsactions;
    }

    private String getProductName(long id) throws UnknownHostException {
        String name;
        try {
            WebClient client = webClientBuilder.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                    .baseUrl("http://businessdomain-product/paymentProduct")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultUriVariables(Collections.singletonMap("url", "http://businessdomain-product/paymentProduct"))
                    .build();
            JsonNode block = client.method(HttpMethod.GET).uri("/product/" + id)
                    .retrieve().bodyToMono(JsonNode.class).block();
            name = block.get("name").asText();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "";
            } else {
                throw new UnknownHostException(e.getMessage());
            }
        }
        return name;
    }
}
