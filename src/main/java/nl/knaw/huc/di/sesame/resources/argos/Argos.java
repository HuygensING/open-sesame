package nl.knaw.huc.di.sesame.resources.argos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Path("argos")
@Produces(MediaType.APPLICATION_JSON)
public class Argos {
  private static final Logger LOG = LoggerFactory.getLogger(Argos.class);

  private static final String EXT_JPG = ".jpg";
  private static final String EXT_HOCR = ".hocr";
  private static final String HOCR_FILE_LOCATION = "/proofreader"; // TODO: expose as configuration parameter

  private static final Predicate<String> IS_JPG = name -> name.endsWith(EXT_JPG);
  private static final Predicate<String> HAS_OCR_COMPANION_FILE = name -> new File(name + EXT_HOCR).exists();
  private final List<String> documents;

  public Argos() throws IOException {
    documents = collectDocuments();
  }

  private static String extractDocumentId(String name) {
    return name.substring(HOCR_FILE_LOCATION.length(), name.lastIndexOf('.'));
  }

  private List<String> collectDocuments() throws IOException {
    return Files.list(Paths.get(HOCR_FILE_LOCATION))
                .map(java.nio.file.Path::toString)
                .filter(IS_JPG)
                .filter(HAS_OCR_COMPANION_FILE)
                .map(Argos::extractDocumentId)
                .limit(5)
                .collect(toList());
  }

  @GET
  @Path("docs")
  public List<String> listDocs() throws IOException {
    return documents;
  }

}
