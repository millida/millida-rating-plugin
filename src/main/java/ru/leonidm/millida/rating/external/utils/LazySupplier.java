package ru.leonidm.millida.rating.external.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * @author LeonidM
 */
public final class LazySupplier<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    private T value;

    @NotNull
    @Contract("_ -> new")
    public static <T> LazySupplier<T> of(@NotNull Supplier<T> supplier) {
        return new LazySupplier<>(supplier);
    }

    private LazySupplier(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @NotNull
    public T get() {
        if (value == null) {
            value = supplier.get();
        }

        return value;
    }
}
