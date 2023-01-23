package ru.itmo.zavar.exception;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import ru.itmo.zavar.Translator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InvalidStringTest {
    @Test
    public void invalidVarNameTest() throws URISyntaxException, IOException {
        System.out.println("Test for invalid string...");
        URL srcUrl = getClass().getClassLoader().getResource("invalidString.zorth"); // path for source code
        assert srcUrl != null;
        Path progPath = Paths.get(srcUrl.toURI());

        Path home = Path.of(System.getProperty("user.home"));
        String[] args = {"-i", progPath.toString(), "-o", String.valueOf(home), "-f", "mne", "-d", "false"};
        Assertions.assertThrows(InvalidStringException.class, () -> {
            Translator.main(args);
        });

    }
}
