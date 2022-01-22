package br.com.coffeeandit.transaction.config;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

public class DomainBusinessException extends ResponseStatusException {

    public DomainBusinessException(HttpStatus status, @Nullable String reason) {
        super(status, reason);

    }

    public DomainBusinessException(@Nullable String reason) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, reason);

    }


    public DomainBusinessException(HttpStatus status) {
        super(status);
    }
}