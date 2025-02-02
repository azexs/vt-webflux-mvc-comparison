package pw.mgr.mvc_2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pw.mgr.mvc_2.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {}
