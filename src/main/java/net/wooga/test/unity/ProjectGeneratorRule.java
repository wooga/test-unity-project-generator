/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wooga.test.unity;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ProjectGeneratorRule extends ExternalResource {

    @Override
    protected void before() throws Throwable {
        super.before();
        unzip();
        writeEditorVersion();
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

    @Override
    public Statement apply(Statement base, Description description) {
        return super.apply(base, description);

    }

    private void writeEditorVersion() throws FileNotFoundException, UnsupportedEncodingException {
        File editorVersionFile = new File(projectDir,"ProjectSettings/ProjectVersion.txt");
        if(getProjectVersion() != null && editorVersionFile.exists()) {
            PrintWriter writer = new PrintWriter(editorVersionFile, "UTF-8");
            writer.println("m_EditorVersion: " + getProjectVersion());
            writer.close();
        }
    }

    private boolean started;
    private String projectVersion;

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) throws FileNotFoundException, UnsupportedEncodingException {
        this.projectVersion = projectVersion;
        writeEditorVersion();
    }

    private final List<File> fileList;
    private File projectDir;

    public File getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(File projectDir) throws IOException {
        if(this.projectDir != null && projectDir != null && projectDir.exists() && projectDir.isDirectory()) {
            FileUtils.copyDirectory(this.projectDir, projectDir);
        }
        this.projectDir = projectDir;
    }

    private static File temporaryDir;

    public ProjectGeneratorRule(File projectDir) {
        this.projectDir = projectDir;
        this.fileList = new ArrayList<>();
    }

    public ProjectGeneratorRule() throws IOException {
        this.projectDir = Files.createTempDirectory("ProjectGeneratorRuleProject").toFile();
        this.fileList = new ArrayList<>();
    }

    private void unzip() throws IOException, ZipException {
        String name = "projectTemplate.zip";
        if (temporaryDir == null) {
            temporaryDir = Files.createTempDirectory("ProjectGeneratorRuleUnpack").toFile();
            temporaryDir.deleteOnExit();
        }

        File temp = new File(temporaryDir, "projectTemplate.zip");
        try (InputStream is = ProjectGeneratorRule.class.getResourceAsStream("/" + name)) {
            Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            FileUtils.forceDelete(temp);
            throw e;
        } catch (NullPointerException e) {
            FileUtils.forceDelete(temp);
            throw new FileNotFoundException("File " + name + " was not found inside JAR.");
        }

        String source = temp.getPath();
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
