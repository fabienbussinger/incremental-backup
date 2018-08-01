package fr.lespoulpes.backup.incremental;

import fr.lespoulpes.backup.incremental.file.RegistryFilenameUtils;
import fr.lespoulpes.backup.incremental.file.diff.DiffProcessing;
import fr.lespoulpes.backup.incremental.file.diff.serder.CSVRegistryDiffDeserializer;
import fr.lespoulpes.backup.incremental.file.seeking.FileFilter;
import fr.lespoulpes.backup.incremental.file.seeking.FileSeeker;
import fr.lespoulpes.backup.incremental.file.seeking.FileSeekerFJP;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.DiffRegistry;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryToRegistryAdapter;
import fr.lespoulpes.backup.incremental.registry.RegistryBuilder.Registry;
import fr.lespoulpes.backup.incremental.tree.Node;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class Backup implements IncrementalExecution {
    private static final Logger LOG = LogManager.getLogger(Backup.class);
    private final CommandLine cmd;

    public Backup(CommandLine cmd) {
        this.cmd = cmd;
    }

    private static void tracePath(Node root) {
        LOG.debug("{} \t\t\t\t{}", root.getNode(), root.getHash());
        root.getChildren().forEach(Backup::tracePath);
    }

    private static long traceTotalSize(Node root) {
        long sum = root.getSize();
        for (Node child : root.getChildren()) {
            sum += traceTotalSize(child);
        }
        return sum;
    }

    @Override
    public void execute() {
        final int compressionLevel = Integer.parseInt(cmd.getOptionValue("compressionLevel", "0"));
        final String exclusions = cmd.getOptionValue("exclusions");
        File source = new File(cmd.getOptionValue("source"));
        File destination = new File(cmd.getOptionValue("destination"));
        if (destination.exists() && !destination.isDirectory()) {
            throw new IllegalArgumentException("Destination must be a folder");
        }
        long start = System.currentTimeMillis();
        final String hashAlgorithm = cmd.getOptionValue("hashAlgorithm");
        FileFilter fileFilter = new FileFilter(Arrays.stream(Optional.ofNullable(StringUtils.split(exclusions, ",")).orElse(new String[0])).map(StringUtils::trimToEmpty).collect(Collectors.toSet()));
        FileSeeker fs = new FileSeekerFJP(source, hashAlgorithm, fileFilter);
        Node root = fs.read();
        long end = System.currentTimeMillis();
        tracePath(root);
        LOG.info("Time spent seeking : {} ms", () -> (end - start));
        LOG.info("Total size : {}", () -> traceTotalSize(root));

        Registry current = Registry.builder().sourceDir(source.toPath()).timestamp(System.currentTimeMillis())
                .hashAlgorithm(hashAlgorithm).node(root).build();

        // find latest registry in destination folder
        if (!destination.exists()) {
            destination.mkdirs();
        }
        Optional<String> latestRegistry = FileUtils.listFiles(destination, new String[]{"registry"}, false).stream()
                .map(File::getName).sorted(RegistryFilenameUtils.filenameComparator(true)).findFirst();

        Registry older = latestRegistry.isPresent()
                ? new DiffRegistryToRegistryAdapter(
                new CSVRegistryDiffDeserializer(new File(destination, latestRegistry.get())).deserialize())
                : Registry.EMPTY;

        DiffRegistry registryDiff = DiffRegistry.fromTwoRegistry().older(older).newer(current).build();
        new DiffProcessing(compressionLevel, destination).process(registryDiff);
    }
}
