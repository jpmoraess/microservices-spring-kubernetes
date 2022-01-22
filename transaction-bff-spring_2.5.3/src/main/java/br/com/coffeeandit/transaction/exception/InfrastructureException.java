package br.com.coffeeandit.transaction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Objects;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InfrastructureException extends RuntimeException {

    private String message;

    public InfrastructureException(final Throwable e) {
        super(e);
        if (Objects.nonNull(e)) {
            this.message = e.getMessage();
        }
    }
    public InfrastructureException(final String message, Throwable throwable) {
        super(message, throwable);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
