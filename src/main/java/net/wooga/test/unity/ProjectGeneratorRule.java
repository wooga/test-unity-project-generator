package net.wooga.test.unity;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ProjectGeneratorRule extends ExternalResource {

    @Override
    protected void before() throws Throwable {
        super.before();
        unzip();
        writeEditorVersion();
    }

    private void writeEditorVersion() throws FileNotFoundException, UnsupportedEncodingException {
        File editorVersionFile = new File(projectDir,"ProjectSettings/ProjectVersion.txt");
        if(getProjectVersion() != null && editorVersionFile.exists()) {
            PrintWriter writer = new PrintWriter(editorVersionFile, "UTF-8");
            writer.println("m_EditorVersion: " + getProjectVersion());
            writer.close();
        }
    }

    @Override
    protected void after() {
        super.after();

        try {
            cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String projectVersion;

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) throws FileNotFoundException, UnsupportedEncodingException {
        this.projectVersion = projectVersion;
        writeEditorVersion();
    }

    private final List<File> fileList;
    private final File projectDir;

    public ProjectGeneratorRule(File projectDir) {
        this.projectDir = projectDir;
        this.fileList = new ArrayList<>();
    }

    private void unzip() throws IOException, ZipException {
        URL templates = this.getClass().getClassLoader().getResource("projectTemplate.zip");

        if(templates == null) {
            throw new IOException("Unable to load project templates");
        }

        String source = templates.getPath();
        File tempCopyDestination = Files.createTempDirectory("ProjectGeneratorRule").toFile();
        ZipFile zipFile = new ZipFile(source);
        zipFile.extractAll(tempCopyDestination.getPath());

        File[] fileList = tempCopyDestination.listFiles();

        if (fileList != null) {
            for (File fileToCopy : fileList) {
                FileUtils.copyToDirectory(fileToCopy, projectDir);
                this.fileList.add(new File(projectDir, fileToCopy.getName()));
            }
        }
    }

    private void cleanup() throws IOException {
        for (File file : fileList) FileUtils.forceDelete(file);
        fileList.clear();
    }
}
