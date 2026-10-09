package ec.com.leodev.paymentchain.transactions.entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String reference;
    private String ibanAccount;
    private LocalDateTime date;
    private Double amount;
    private Double fee;//comision de la transaccion
    private String description;
    private String status;//01:Pendiente  02:Liquidada  03:Rechazada 04:Cancelada
    private String channel;//WEB  CAJERO   OFICINA
}
