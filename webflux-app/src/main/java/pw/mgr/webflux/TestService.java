package pw.mgr.webflux;

import org.springframework.stereotype.Service;
import pw.mgr.webflux.repository.ProductRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;

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

  private static final int BUFFER_SIZE = 4096;
  private static final int TOTAL_SIZE = 4096;

  private final ProductRepository productRepository;

  public TestService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public Mono<Void> queryDatabase() {
    return productRepository.findAll().collectList().then();
  }

  public Mono<Void> performIOOperation(Path directory) {
    return Mono.fromCallable(() -> Files.createDirectories(directory))
        .then(generateFileName(directory))
        .flatMap(this::writeLargeFile)
        .flatMap(this::readLargeFile)
        .flatMap(this::deleteFile)
        .timeout(Duration.ofSeconds(30))
        .doOnError(e -> System.err.println(
            "[ERROR] Operation failed: " + e.getMessage()));
  }

  private Mono<Path> generateFileName(Path directory) {
    return Mono.fromCallable(() -> directory.resolve(
        "testfile_" + UUID.randomUUID() + ".dat"));
  }

  private Mono<Path> writeLargeFile(Path filePath) {
    return Mono.using(
        () -> AsynchronousFileChannel.open(filePath, StandardOpenOption.CREATE,
            StandardOpenOption.WRITE),
        fileChannel -> {
          Random random = new Random();
          Flux<ByteBuffer> buffers = Flux.range(0, TOTAL_SIZE / BUFFER_SIZE)
              .map(i -> {
                byte[] bytes = new byte[BUFFER_SIZE];
                random.nextBytes(bytes);
                return ByteBuffer.wrap(bytes);
              });

          return buffers
              .concatMap(buffer -> writeToFile(fileChannel, buffer))
              .then(Mono.just(filePath));
        },
        this::closeChannel
    );
  }

  private Mono<Void> writeToFile(AsynchronousFileChannel fileChannel,
                                 ByteBuffer buffer) {
    return Mono.create(sink -> fileChannel.write(buffer, buffer.position(),
        buffer, new CompletionHandler<Integer, ByteBuffer>() {
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
        this::closeChannel
    );
  }

  private Mono<Void> readFromFile(AsynchronousFileChannel fileChannel) {
    return Mono.create(sink -> {
      ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
      readChunk(fileChannel, buffer, 0, sink);
    });
  }

  private void readChunk(AsynchronousFileChannel fileChannel, ByteBuffer buffer,
                         long position, MonoSink<Void> sink) {
    fileChannel.read(buffer, position, buffer, new CompletionHandler<>() {
      @Override
      public void completed(Integer bytesRead, ByteBuffer attachment) {
        if (bytesRead == -1) {
          sink.success();
        } else {
          buffer.clear();
          readChunk(fileChannel, buffer, position + bytesRead, sink);
        }
      }

      @Override
      public void failed(Throwable exc, ByteBuffer attachment) {
        sink.error(exc);
      }
    });
  }

  private Mono<Void> deleteFile(Path filePath) {
    return Mono.fromRunnable(() -> {
      try {
        Files.deleteIfExists(filePath);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).subscribeOn(Schedulers.boundedElastic()).then();
  }

  private void closeChannel(AsynchronousFileChannel channel) {
    try {
      channel.close();
    } catch (IOException e) {
      System.err.println("[ERROR] Failed to close channel: " + e.getMessage());
    }
  }
}
