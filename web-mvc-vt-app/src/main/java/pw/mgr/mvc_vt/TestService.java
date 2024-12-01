package pw.mgr.mvc_vt;

import org.springframework.stereotype.Service;
import pw.mgr.mvc_vt.entity.Product;
import pw.mgr.mvc_vt.repository.ProductRepository;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class TestService {

    private static final int BUFFER_SIZE =  512;
    private static final int TOTAL_SIZE =  1024;

    private final ProductRepository productRepository;

    public TestService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> queryDatabase() {
        return productRepository.findAll();
    }

    public void performIOOperation(Path directory) throws IOException {
        Files.createDirectories(directory);

        String fileName = "testfile_" + UUID.randomUUID() + ".dat";
        Path filePath = directory.resolve(fileName);

        writeLargeFile(filePath);

        readLargeFile(filePath);

        Files.deleteIfExists(filePath);
    }

    private void writeLargeFile(Path filePath) throws IOException {
        Random random = new Random();
        byte[] buffer = new byte[BUFFER_SIZE];
        try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
            int bytesWritten = 0;
            while (bytesWritten < TOTAL_SIZE) {
                random.nextBytes(buffer);
                outputStream.write(buffer);
                bytesWritten += BUFFER_SIZE;
            }
        }
    }

    private void readLargeFile(Path filePath) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        try (FileInputStream inputStream = new FileInputStream(filePath.toFile())) {
            while (inputStream.read(buffer) != -1) {
            }
        }
    }

}

