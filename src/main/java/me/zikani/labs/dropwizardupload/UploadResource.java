package me.zikani.labs.dropwizardupload;

import io.dropwizard.auth.Auth;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonMap;

@Path("/upload")
@Produces(MediaType.APPLICATION_JSON)
public class UploadResource {

    private String uploadDir;

    public UploadResource(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @GET
    public Response getUploadedFilesInfo() throws IOException {
        final List<FileInfo> files = new ArrayList<>();

        Files.walkFileTree(Paths.get(getUploadDir()), new SimpleFileVisitor<java.nio.file.Path>() {
            @Override
            public FileVisitResult visitFile(java.nio.file.Path path, BasicFileAttributes attrs) throws IOException {
                File file = path.toFile();
                LocalDateTime dt = LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.of("UTC"));
                files.add(new FileInfo(file.getName(), attrs.size(), dt.toLocalDate()));
                return FileVisitResult.CONTINUE;
            }
        });

        return Response.ok(Collections.singletonMap("files", files)).build();
    }

    @Path("/auth")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response authenticatedUpload(
            @Auth User user,
            @FormDataParam("fileData")FormDataContentDisposition contentDisposition,
            @FormDataParam("fileData")InputStream inputStream) {

        if (user == null) return DefaultUnauthorizedHandler.notAuthorized();

        return handleUpload(contentDisposition, inputStream);
    }

    @Path("/noauth")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response unauthenticatedUpload(@FormDataParam("fileData")FormDataContentDisposition contentDisposition,
                                          @FormDataParam("fileData")InputStream inputStream) {

        return handleUpload(contentDisposition, inputStream);
    }

    private Response handleUpload(ContentDisposition contentDisposition,InputStream inputStream) {
        String fileName = contentDisposition.getFileName();
        String finalName = UUID.randomUUID().toString().concat(fileName);
        try {
            Files.copy(inputStream, Paths.get(getUploadDir(), finalName));
            return Response.ok(singletonMap("message", "File Uploaded Successfully"))
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    private String getUploadDir() {
        return uploadDir;
    }
}
