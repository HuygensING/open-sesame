package nl.knaw.huc.di.sesame.resources.argos;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import nl.knaw.huc.di.sesame.util.Streamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Path("argos")
public class Argos {
  private static final Logger LOG = LoggerFactory.getLogger(Argos.class);

  private static final String EXT_JPG = ".jpg";
  private static final String EXT_HOCR = ".hocr";

  private static final Predicate<String> IS_JPG = name -> name.endsWith(EXT_JPG);
  private static final Predicate<String> HAS_HOCR_COMPANION = name -> Files.exists(Paths.get(name + EXT_HOCR));
  private final JsonFactory factory;
  private final String baseDir;
  private final Streamer streamer;

  public Argos(JsonFactory factory, String baseDir) {
    this.factory = factory;
    this.baseDir = baseDir;
    this.streamer = new Streamer(baseDir);
  }

  private String extractDocumentId(String name) {
    return name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
  }

  private Stream<String> streamDocumentIds() throws IOException {
    return Files.list(Paths.get(baseDir))
                .map(java.nio.file.Path::toString)
                .filter(IS_JPG)
                .filter(HAS_HOCR_COMPANION)
                .map(this::extractDocumentId);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public StreamingOutput listDocs() {
    return output -> {
      final JsonGenerator generator = factory.createGenerator(output);
      generator.writeStartArray();
      streamDocumentIds().forEach(documentId -> writeString(generator, documentId));
      generator.writeEndArray();
      generator.flush();
    };
  }

  private void writeString(JsonGenerator generator, String documentId) {
    try {
      generator.writeString(documentId);
    } catch (IOException e) {
      LOG.debug("Failed to write: {}", documentId);
    }
  }

  @GET
  @Path("{id}/hocr")
  @Produces(MediaType.APPLICATION_XHTML_XML)
  public StreamingOutput getHocr(@PathParam("id") String documentId) {
    return streamer.stream(documentId + EXT_JPG + EXT_HOCR);
  }

  @GET
  @Path("{id}/image")
  @Produces("image/jpeg")
  public StreamingOutput getImage(@PathParam("id") String documentId) {
    return streamer.stream(documentId + EXT_JPG);
  }

  @PUT
  @Path("{id}/text")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response putText(@PathParam("id")String documentId, @FormParam("hocr") String text) throws IOException {
    LOG.trace("document: {}, text: {}", documentId, text.substring(0, 1000) + "...");
    Files.write(Paths.get("/tmp", documentId + ".xml"), Collections.singleton(text));
    return Response.noContent().build();
  }

}
