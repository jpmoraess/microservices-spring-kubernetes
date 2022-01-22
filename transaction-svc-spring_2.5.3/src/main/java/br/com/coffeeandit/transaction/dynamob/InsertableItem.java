package br.com.coffeeandit.transaction.dynamob;

import com.amazonaws.services.dynamodbv2.document.Item;

public interface InsertableItem<E> {


    Item putItem(E item);
}
