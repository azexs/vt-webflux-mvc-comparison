package pw.mgr.webflux.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pw.mgr.webflux.entity.Product;

public interface ProductRepository extends ReactiveCrudRepository<Product, Integer> {}
