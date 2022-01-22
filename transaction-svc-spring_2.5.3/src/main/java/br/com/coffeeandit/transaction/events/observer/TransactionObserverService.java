package br.com.coffeeandit.transaction.events.observer;

import java.util.ArrayList;
import java.util.List;

public class TransactionObserverService<E> {

    private List<TransactionObserver> observers = new ArrayList<>();

        public void addObserver(TransactionObserver transactionObserver) {
        observers.add(transactionObserver);
    }

    public void notification(E item) {
        observers.parallelStream().forEach(transactionObserver -> {
            transactionObserver.doObserver(item);
        });
    }
}
