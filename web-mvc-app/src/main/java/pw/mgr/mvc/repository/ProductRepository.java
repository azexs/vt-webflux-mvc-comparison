package pw.mgr.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pw.mgr.mvc.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {}
