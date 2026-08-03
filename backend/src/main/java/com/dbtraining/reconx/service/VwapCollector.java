package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.EquityTrade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class VwapCollector implements Collector<EquityTrade, VwapCollector.Accumulator, BigDecimal> {

    static class Accumulator {
        BigDecimal weightedPrice = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;
    }

    @Override
    public Supplier<Accumulator> supplier() {
        return Accumulator::new;
    }

    @Override
    public BiConsumer<Accumulator, EquityTrade> accumulator() {
        return (acc, trade) -> {
            acc.weightedPrice = acc.weightedPrice.add(
                    trade.price().multiply(trade.quantity())
            );

            acc.totalQuantity = acc.totalQuantity.add(
                    trade.quantity()
            );
        };
    }

    @Override
    public BinaryOperator<Accumulator> combiner() {
        return (left, right) -> {
            Accumulator result = new Accumulator();

            result.weightedPrice =
                    left.weightedPrice.add(right.weightedPrice);

            result.totalQuantity =
                    left.totalQuantity.add(right.totalQuantity);

            return result;
        };
    }

    @Override
    public Function<Accumulator, BigDecimal> finisher() {
        return acc -> {
            if (acc.totalQuantity.signum() == 0) {
                return BigDecimal.ZERO;
            }

            return acc.weightedPrice.divide(
                    acc.totalQuantity,
                    6,
                    RoundingMode.HALF_UP
            );
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.singleton(Characteristics.UNORDERED);
    }
}