package ru.itmo.zavar.exception;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import ru.itmo.zavar.Translator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EmptyFileTest {

    @Test
    public void emptyTest() throws URISyntaxException, IOException {
        System.out.println("Test for empty file...");
        URL srcUrl = getClass().getClassLoader().getResource("empty.zorth"); // path for source code
        assert srcUrl != null;
        Path progPath = Paths.get(srcUrl.toURI());

        Path home = Path.of(System.getProperty("user.home"));
        String[] args = {"-i", progPath.toString(), "-o", String.valueOf(home), "-f", "mne", "-d", "false"};
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Translator.main(args);
        });

    }

}
