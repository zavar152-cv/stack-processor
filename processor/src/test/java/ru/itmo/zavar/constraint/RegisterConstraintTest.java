package ru.itmo.zavar.constraint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.base.register.Register;
import ru.itmo.zavar.exception.RegisterConstraintException;

public class RegisterConstraintTest {

    @Test
    public void registerTest() {
        System.out.println("Testing register constraint...");
        Register<Integer> register = new Register<>(2, -2);
        Assertions.assertThrows(RegisterConstraintException.class, () -> {
            register.writeValue(10);
        });
        Assertions.assertThrows(RegisterConstraintException.class, () -> {
            register.writeValue(-10);
        });
    }
}
