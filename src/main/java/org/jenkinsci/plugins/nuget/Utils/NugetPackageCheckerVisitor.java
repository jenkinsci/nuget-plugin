package org.jenkinsci.plugins.nuget.Utils;

import com.google.common.collect.Maps;
import hudson.FilePath;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.nuget.NugetGlobalConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;

/**
 * @author Arnaud TAMAILLON
 */
class NugetPackageCheckerVisitor extends SimpleFileVisitor<Path> {

    private final Map<String, String> latestPackageVersions = Maps.newHashMap();
    private final XTriggerLog log;
    private final boolean preReleaseChecked;
    private final FilePath workspaceRoot;
    private final DocumentBuilder builder;
    private final NugetGlobalConfiguration configuration;
    private boolean updated;

    boolean isUpdated() {
        return updated;
    }

    NugetPackageCheckerVisitor(XTriggerLog log, NugetGlobalConfiguration configuration, boolean preReleaseChecked, FilePath workspaceRoot) throws ParserConfigurationException {
        this.log = log;
        this.configuration = configuration;
        this.preReleaseChecked = preReleaseChecked;
        this.workspaceRoot = workspaceRoot;
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        Path fileName = file.getFileName();
        if (fileName == null) {
            return FileVisitResult.CONTINUE;
        }
        if (!fileName.toString().equalsIgnoreCase("packages.config")) {
            return FileVisitResult.CONTINUE;
        }
        return checkPackageFile(file);
    }

    private FileVisitResult checkPackageFile(Path file) throws IOException {
        log.info(String.format("Checking packages file: %s", file.toAbsolutePath().toString()));
        try {
            Document doc = builder.parse(file.toFile());

            doc.getDocumentElement().normalize();
            NodeList packageNodes = doc.getElementsByTagName("package");
            for (int idx = 0; idx < packageNodes.getLength(); idx++) {
                Element p = (Element) packageNodes.item(idx);
                String id = p.getAttribute("id");
                String version = p.getAttribute("version");
                String latest = getPackageVersion(workspaceRoot, id);

                if (latest == null || !version.equals(latest)) {
                    log.info(String.format("Package %s v%s should update to v%s.", id, version, latest));
                    updated = true;
                    return FileVisitResult.TERMINATE;
                }
            }
        } catch (SAXException ex) {
            log.error(ex.toString());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        log.error(exc.toString());
        return FileVisitResult.CONTINUE;
    }

    private String getPackageVersion(FilePath workspaceRoot, String packageName) throws IOException {
        if (latestPackageVersions.containsKey(packageName)) {
            return latestPackageVersions.get(packageName);
        }
        NugetGetLatestPackageVersionCommand command = new NugetGetLatestPackageVersionCommand(log, configuration, workspaceRoot, packageName, preReleaseChecked);
        command.execute();
        return command.getVersion();
    }
}
