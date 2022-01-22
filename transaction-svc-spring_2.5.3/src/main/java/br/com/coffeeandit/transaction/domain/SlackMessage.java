package br.com.coffeeandit.transaction.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class SlackMessage {



    public SlackMessage() {

    }

    private String title;
    private List<Field> fields = new ArrayList<>();
    @JsonProperty("author_name")
    private String author;
    @JsonProperty("author_icon")
    private String authorIcon;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorIcon() {
        return authorIcon;
    }

    public void setAuthorIcon(String authorIcon) {
        this.authorIcon = authorIcon;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @JsonProperty("image_url")
    private String imageUrl;



    public void addField(final String name, final String value) {

    fields.add(new Field(name, value, true));

    }
    class Field {

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isShortValue() {
            return shortValue;
        }

        public void setShortValue(boolean shortValue) {
            this.shortValue = shortValue;
        }

        private String title;
        private String value;
        @JsonProperty("short")
        private boolean shortValue;


        public Field(String title, final String value, boolean shortValue) {
            this.title = title;
            this.value = value;
            this.shortValue = shortValue;
        }
    }
}
