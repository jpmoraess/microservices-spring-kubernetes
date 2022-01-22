package br.com.coffeeandit.transaction.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TemplateSlack {


    public TemplateSlack() {

    }


    private String text;
    private List<SlackMessage> attachments = new ArrayList<>();


}
