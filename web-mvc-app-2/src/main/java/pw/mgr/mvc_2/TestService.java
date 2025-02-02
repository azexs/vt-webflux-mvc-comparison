package pw.mgr.mvc_2;

import org.springframework.stereotype.Service;
import pw.mgr.mvc_2.entity.Product;
import pw.mgr.mvc_2.repository.ProductRepository;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@Service
public class TestService {

    private static final int BUFFER_SIZE = 4096;
    private static final int TOTAL_SIZE = 4096;

    private final Semaphore dbSemaphore = new Semaphore(100);

    private final ProductRepository productRepository;

    public TestService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> queryDatabase() {
        try {
            dbSemaphore.acquire();
            return productRepository.findAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            dbSemaphore.release();
        }
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
        try (SeekableByteChannel channel = Files.newByteChannel(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            int bytesWritten = 0;
            while (bytesWritten < TOTAL_SIZE) {
                random.nextBytes(buffer);
                byteBuffer.clear();
                channel.write(byteBuffer);
                bytesWritten += BUFFER_SIZE;
            }
        }
    }

    private void readLargeFile(Path filePath) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        try (SeekableByteChannel channel = Files.newByteChannel(filePath, StandardOpenOption.READ)) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            while (channel.read(byteBuffer) != -1) {
                byteBuffer.clear();
            }
        }
    }
}
