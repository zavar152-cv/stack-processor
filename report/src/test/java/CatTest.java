import org.junit.jupiter.api.Test;
import ru.itmo.zavar.Launcher;
import ru.itmo.zavar.Processor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CatTest {
    @Test
    public void launch() throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource("cat.zorth");
        assert resource != null;
        Path path = Paths.get(resource.toURI());
        Path home = Path.of(System.getProperty("user.home"));
        String[] argsLauncher = {"-i", path.toString(), "-o", String.valueOf(home), "-f", "bin", "-d", "true"};
        Launcher.main(argsLauncher);

        //-p C:\Users\yarus\compiled.bin -d C:\Users\yarus\data.dbin -dg true -i C:\Users\yarus\input
        Files.deleteIfExists(home.resolve("input"));
        Files.createFile(home.resolve("input"));
        Files.writeString(home.resolve("input"), "hello cat!", StandardOpenOption.APPEND);
        String[] argsProcessor = {"-p", home.resolve("compiled.bin").toString(), "-d",
                home.resolve("data.dbin").toString(), "-dg", "true", "-i", home.resolve("input").toString()};
        Processor.main(argsProcessor);
    }
}
