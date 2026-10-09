package ec.com.leodev.paymentchain.customer.service;

import com.fasterxml.jackson.databind.JsonNode;
import ec.com.leodev.paymentchain.customer.repository.ICustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BusinessTransactionTwoService {

    @Autowired
    ICustomerRepository customerRepository;

    private final WebClient.Builder webClientBuilder;

    public BusinessTransactionTwoService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    HttpClient httpClient = HttpClient
            .create()
            //Connection Timeout: is a period within which a connection between a client and a server must be established
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(EpollChannelOption.TCP_KEEPIDLE, 300)
            .option(EpollChannelOption.TCP_KEEPINTVL, 60)
            //Response Timeout: The maximun time we wait to receive a response after sending a request
            .responseTimeout(Duration.ofSeconds(1))
            // Read and Write Timeout: A read timeout occurs when no data was read within a certain
            //period of time, while the write timeout when a write operation cannot finish at a specific time
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

    public String getNameProduct(Long id) throws UnknownHostException {
        try{
            WebClient webClient = webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient.wiretap(true)))
                    .baseUrl("http://businessdomain-product/paymentProduct")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultUriVariables(Collections.singletonMap("url", "http://businessdomain-product/paymentProduct"))
                    .build();
            JsonNode product = webClient.get()
                    .uri("/product/" + id)
                    .retrieve()
                    .bodyToMono(JsonNode.class).block();

            return product.get("name").asText();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "";
            } else {
                throw new UnknownHostException(e.getMessage());
            }
        }
    }

    public  <T> List<T> getTransactions(String idBan) {
        List<T> transactions = new ArrayList<>();
        try {
            WebClient webClient = webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient.wiretap(true)))
                    .baseUrl("http://businessdomain-transactions/paymentTransaction")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultUriVariables(Collections.singletonMap("url", "http://businessdomain-transactions/paymentTransaction"))
                    .build();
            List<Object> listTransactions = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/transaction/transactionsByIbaAccount").queryParam("ibanAccount", idBan).build())
                    .retrieve()
                    .bodyToFlux(Object.class).collectList().block();
            transactions = (List<T>) listTransactions;
        } catch (Exception e) {
            return transactions;
        }
        return transactions;
    }

}
