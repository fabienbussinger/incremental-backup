package fr.lespoulpes.backup.incremental.file.seeking;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Set;
import java.util.function.Predicate;

public class FileFilter implements Predicate<File> {
    private final Set<String> patterns;

    public FileFilter(Set<String> patterns) {
        this.patterns = patterns;
    }


    @Override
    public boolean test(File file) {
        for (String pattern : this.patterns) {
            if (FilenameUtils.wildcardMatch(file.getName(), pattern)) {
                return true;
            }
        }
        return false;
    }
}
