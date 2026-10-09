package ec.com.leodev.paymentchain.products.repository;

import ec.com.leodev.paymentchain.products.entitys.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author emedina
 */
public interface IProductRepository extends JpaRepository<Product, Long> {
}
