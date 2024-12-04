package pw.mgr.webflux;

import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.stereotype.Service;
import pw.mgr.webflux.repository.ProductRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@Service
public class TestService {

    private static final int BUFFER_SIZE = 512;
    private static final int TOTAL_SIZE = 1024;

    private final ProductRepository productRepository;
    private final DataBufferFactory bufferFactory;

    public TestService(ProductRepository productRepository, DataBufferFactory bufferFactory) {
        this.productRepository = productRepository;
        this.bufferFactory = bufferFactory;
    }

    public Mono<Void> queryDatabase() {
        return productRepository.findAll().then();
    }

    public Mono<Void> performIOOperation(Path directory) {
        return Mono.fromCallable(() -> Files.createDirectories(directory))
                .then(generateFileName(directory))
                .flatMap(this::writeLargeFile)
                .flatMap(this::readLargeFile)
                .flatMap(this::deleteFile)
                .timeout(Duration.ofSeconds(30))
                .doOnError(e -> System.err.println("[ERROR] Operation failed: " + e.getMessage()));
    }

    private Mono<Path> generateFileName(Path directory) {
        return Mono.fromCallable(() -> directory.resolve("testfile_" + UUID.randomUUID() + ".dat"));
    }

    private Mono<Path> writeLargeFile(Path filePath) {
        return Mono.using(
                () -> AsynchronousFileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE),
                fileChannel -> Flux.range(0, TOTAL_SIZE / BUFFER_SIZE)
                        .concatMap(index -> {
                            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                            byte[] randomBytes = new byte[BUFFER_SIZE];
                            new Random().nextBytes(randomBytes);
                            buffer.put(randomBytes);
                            buffer.flip();
                            return writeToFile(fileChannel, buffer, index * BUFFER_SIZE);
                        })
                        .then(Mono.just(filePath)),
                fileChannel -> {
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        System.err.println("[ERROR] Failed to close AsynchronousFileChannel: " + e.getMessage());
                    }
                }
        );
    }

    private Mono<Void> writeToFile(AsynchronousFileChannel fileChannel, ByteBuffer buffer, long position) {
        return Mono.create(sink -> fileChannel.write(buffer, position, buffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                sink.success();
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                sink.error(exc);
            }
        }));
    }


    private Mono<Path> readLargeFile(Path filePath) {
        return Mono.using(
                () -> AsynchronousFileChannel.open(filePath, StandardOpenOption.READ),
                fileChannel -> readFromFile(fileChannel)
                        .then(Mono.just(filePath)),

                fileChannel -> {
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        System.err.println("[ERROR] Failed to close AsynchronousFileChannel: " + e.getMessage());
                    }
                }
        );
    }

    private Mono<Void> readFromFile(AsynchronousFileChannel fileChannel) {
        return Mono.fromCallable(() -> {
                    try {
                        return fileChannel.size(); // Wywołanie opakowane w try-catch
                    } catch (IOException e) {
                        throw new RuntimeException("[ERROR] Failed to get file size: " + e.getMessage(), e);
                    }
                })
                .flatMapMany(size -> Flux.create(emitter -> {
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    fileChannel.read(buffer, 0, buffer, new CompletionHandler<>() {
                        private long position = 0; // Aktualna pozycja w pliku

                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            if (result == -1 || position >= size) {
                                emitter.complete();
                                return;
                            }
                            buffer.flip();
                            emitter.next(buffer);
                            buffer.clear();
                            position += result;
                            fileChannel.read(buffer, position, buffer, this);
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            emitter.error(exc);
                        }
                    });
                }))
                .then();
    }


    private Mono<Void> deleteFile(Path filePath) {
        return Mono.fromRunnable(() -> {
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to delete file: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
