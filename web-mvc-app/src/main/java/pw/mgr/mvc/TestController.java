package pw.mgr.mvc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Paths;

@RestController
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello World!";
    }

    @GetMapping("/api/db")
    public String getData() {
        testService.queryDatabase();
        return "OK";
    }

    @GetMapping("/api/file")
    public String getFile() throws IOException {
        testService.performIOOperation(Paths.get("/tmp"));
        return "OK";
    }

    @GetMapping("/api/delay")
    public String getDelay(@RequestParam(name = "delay", required = false, defaultValue = "500") long delay) throws InterruptedException {
        Thread.sleep(delay);
        return "OK";
    }
}
