package nl.knaw.huc.di.sesame.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Streamer {
  private static final Logger LOG = LoggerFactory.getLogger(Streamer.class);

  private final String baseDir;

  public Streamer(String baseDir) {
    this.baseDir = baseDir;
  }

  public StreamingOutput stream(String fileName) {
    return stream(Paths.get(baseDir, fileName));
  }

  private static StreamingOutput stream(Path path) {
    final byte[] buf = new byte[16384];

    return output -> {
      int len;
      try (final InputStream is = Files.newInputStream(path)) {
        while ((len = is.read(buf, 0, buf.length)) != -1) {
          output.write(buf, 0, len);
        }
        output.flush();
      } catch (NoSuchFileException e) {
        LOG.warn("No such file: {}", e.getMessage());
        throw new NotFoundException();
      } catch (IOException e) {
        e.printStackTrace();
        String message = String.format("Error streaming %s: %s", path, e.getMessage());
        LOG.warn(message);
        throw new WebApplicationException(message);
      }
    };
  }
}
