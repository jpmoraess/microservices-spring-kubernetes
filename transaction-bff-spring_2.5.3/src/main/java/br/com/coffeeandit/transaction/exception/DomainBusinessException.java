package br.com.coffeeandit.transaction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
public class DomainBusinessException extends ResponseStatusException {


    private String message;

    public DomainBusinessException(final String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY,  message);
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        HttpStatus status = super.getStatus();
        if (Objects.isNull(status)) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        }
        return status;
    }

    @Override
    public String getReason() {
        return message;
    }
}
