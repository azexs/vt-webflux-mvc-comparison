package pw.mgr.webflux;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;

@RestController
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/api/hello")
    public Mono<String> hello() {
        return Mono.just("Hello World!");
    }

    @GetMapping("/api/db")
    public Mono<String> getData() {
        return testService.queryDatabase()
                .thenReturn("OK"); // Po zakończeniu operacji zwraca "OK"
    }

    @GetMapping("/api/file")
    public Mono<String> getFile() {
        return testService.performIOOperation(Paths.get("/tmp"))
                .thenReturn("OK");
    }

    @GetMapping("/api/delay")
    public Mono<String> getDelay(@RequestParam(name = "delay", required = false, defaultValue = "500") long delay) {
        return Mono.delay(java.time.Duration.ofMillis(delay))
                .then(Mono.just("OK"));
    }
}
