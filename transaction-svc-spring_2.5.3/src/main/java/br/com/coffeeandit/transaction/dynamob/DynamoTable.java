package br.com.coffeeandit.transaction.dynamob;

import com.amazonaws.services.dynamodbv2.document.Table;

public interface DynamoTable<T extends Table> {


    T getTable();
}
