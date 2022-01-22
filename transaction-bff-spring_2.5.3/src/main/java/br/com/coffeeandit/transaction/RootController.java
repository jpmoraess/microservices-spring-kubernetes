package br.com.coffeeandit.transaction;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

@Controller
public class RootController {

    @RequestMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET   )
    @ResponseBody
    public Mono<String> getController() {
        return Mono.just("Olá <b>CoffeeAndIT</b>, para acessar o Swagger clique no link <a href=\"/swagger-ui.html\"> aqui</a>\n");
    }
}
