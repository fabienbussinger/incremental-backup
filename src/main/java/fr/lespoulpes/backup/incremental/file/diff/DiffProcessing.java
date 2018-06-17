package fr.lespoulpes.backup.incremental.file.diff;

import fr.lespoulpes.backup.incremental.file.diff.serder.CSVRegistryDiffSerializer;
import fr.lespoulpes.backup.incremental.registry.DiffRegistryBuilder.DiffRegistry;

import java.io.File;

public class DiffProcessing {
    private final CSVRegistryDiffSerializer csvRegistryDiffSerializer;
    private final RealFileDiffWriter realFileDiffWriter;

    public DiffProcessing(int compressionLevel, File destination) {
        this.csvRegistryDiffSerializer = new CSVRegistryDiffSerializer(destination);
        this.realFileDiffWriter = new RealFileDiffWriter(compressionLevel, destination);
    }

    public void process(DiffRegistry registry) {
        this.csvRegistryDiffSerializer.serialize(registry);
        this.realFileDiffWriter.write(registry);
    }
}
