import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.itmo.zavar.Translator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AllCommandsBinTest {
    @Test
    public void launch() throws URISyntaxException, IOException {
        URL srcUrl = getClass().getClassLoader().getResource("program.zorth"); // path for source code
        assert srcUrl != null;
        Path progPath = Paths.get(srcUrl.toURI());

        URL allCompUrl = getClass().getClassLoader().getResource("allcomp.bin"); // path for compare program
        assert allCompUrl != null;
        Path allCompPath = Paths.get(allCompUrl.toURI());

        URL allDataUrl = getClass().getClassLoader().getResource("alldata.dbin"); // path for compare data
        assert allDataUrl != null;
        Path allDataPath = Paths.get(allDataUrl.toURI());

        Path home = Path.of(System.getProperty("user.home"));
        String[] args = {"-i", progPath.toString(), "-o", String.valueOf(home), "-f", "bin", "-d", "false"};
        Translator.main(args);

        Path compiledPath = home.resolve("compiled.bin"); // path for program
        Path dataPath = home.resolve("data.dbin"); // path for data

        Assertions.assertEquals(-1L, Files.mismatch(compiledPath, allCompPath)); // check for identical programs
        Assertions.assertEquals(-1L, Files.mismatch(dataPath, allDataPath)); // check for identical data
    }
}
