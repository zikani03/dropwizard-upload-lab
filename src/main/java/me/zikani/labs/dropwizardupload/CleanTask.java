package me.zikani.labs.dropwizardupload;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class CleanTask extends Task {
    private final Path directory;
    public CleanTask(Path directoryPath) {
        super("clean-dir");
        this.directory = directoryPath;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> immutableMultimap, PrintWriter printWriter) throws Exception {
        Files.deleteIfExists(directory);
        Files.createDirectory(directory);
    }
}
