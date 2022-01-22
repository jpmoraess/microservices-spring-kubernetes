package br.com.coffeeandit.transaction.config;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

public class NotFoundResponse extends ResponseStatusException {

    public NotFoundResponse(HttpStatus status, @Nullable String reason) {
        super(status, reason);

    }

    public NotFoundResponse(@Nullable String reason) {
        super(HttpStatus.NOT_FOUND, reason);

    }


    public NotFoundResponse(HttpStatus status) {
        super(status);
    }
}