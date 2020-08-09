package com.thoughtworks.rslist.domain;

import lombok.Data;

@Data
public class Trade {
    double amount;
    int rank;

    public Trade(double amount, int rank) {
        this.amount = amount;
        this.rank = rank;
    }
}
