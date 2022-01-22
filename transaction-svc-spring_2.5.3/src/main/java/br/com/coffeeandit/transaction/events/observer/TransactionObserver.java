package br.com.coffeeandit.transaction.events.observer;

public interface TransactionObserver<E> {

    void doObserver(E item);
}
