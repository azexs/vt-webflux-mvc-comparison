package pw.mgr.mvc_vt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pw.mgr.mvc_vt.entity.Product;


public interface ProductRepository extends JpaRepository<Product, Integer> {}
