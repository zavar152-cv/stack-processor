package ru.itmo.zavar.exception;

public class DsEmpty extends ControlUnitException {
    public DsEmpty() {
        super("DS is empty");
    }
}
