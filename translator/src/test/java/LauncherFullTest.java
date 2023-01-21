import org.junit.jupiter.api.Test;
import ru.itmo.zavar.Launcher;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LauncherFullTest {
    @Test
    public void launch() throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource("program.zorth");
        assert resource != null;
        Path path = Paths.get(resource.toURI());
        Path home = Path.of(System.getProperty("user.home"));
        String[] args = {"-i", path.toString(), "-o", String.valueOf(home), "-f", "mne"};
        Launcher.main(args);
    }
}
