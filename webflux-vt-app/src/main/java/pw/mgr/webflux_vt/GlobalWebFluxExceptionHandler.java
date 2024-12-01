package pw.mgr.webflux_vt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalWebFluxExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalWebFluxExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Mono<ErrorResponse> handleAllExceptions(Exception ex, ServerWebExchange exchange) {
        // Log the exception
        logger.error("Exception occurred: ", ex);

        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Build a generic error response
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                ex.getMessage()
        );

        return Mono.just(errorResponse);
    }

    // Custom error response class
    static class ErrorResponse {
        private int status;
        private String message;
        private String details;

        public ErrorResponse(int status, String message, String details) {
            this.status = status;
            this.message = message;
            this.details = details;
        }

        // Getters and setters omitted for brevity
    }
}
