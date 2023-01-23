package ru.itmo.zavar.exception;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import ru.itmo.zavar.Translator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InvalidFuncNameTest {

    @Test
    public void invalidFuncNameTest() throws URISyntaxException, IOException {
        System.out.println("Test for invalid func name...");
        URL srcUrl = getClass().getClassLoader().getResource("invalidFuncName.zorth"); // path for source code
        assert srcUrl != null;
        Path progPath = Paths.get(srcUrl.toURI());

        Path home = Path.of(System.getProperty("user.home"));
        String[] args = {"-i", progPath.toString(), "-o", String.valueOf(home), "-f", "mne", "-d", "false"};
        Assertions.assertThrows(InvalidFunctionNameException.class, () -> {
            Translator.main(args);
        });

    }

}
