package ru.itmo.zavar.base.register;

import lombok.RequiredArgsConstructor;
import ru.itmo.zavar.exception.RegisterConstraintException;

@RequiredArgsConstructor
public final class Register<T extends Number & Comparable<T>> {
    private final T max;
    private final T min;
    private T value;

    public T readValue() {
        return value;
    }

    public void writeValue(final T newValue) throws RegisterConstraintException {
        if (newValue.compareTo(max) > 0) {
            throw new RegisterConstraintException("Provided value %s is greater than max %s".formatted(newValue, max));
        }
        if (newValue.compareTo(min) < 0) {
            throw new RegisterConstraintException("Provided value %s is les than min %s".formatted(newValue, min));
        }
        value = newValue;
    }
}
