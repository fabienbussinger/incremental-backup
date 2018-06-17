package fr.lespoulpes.backup.incremental;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.lespoulpes.backup.incremental.file.RegistryFilenameUtils;
import fr.lespoulpes.backup.incremental.file.zip.UnzipIncrementalArchive;

public class Restore implements IncrementalExecution {
	private static final Logger LOG = LogManager.getLogger(Restore.class);
	private final CommandLine cmd;

	public Restore(CommandLine cmd) {
		this.cmd = cmd;
	}

	@Override
	public void execute() {
		if (!cmd.hasOption("source") || !cmd.hasOption("destination")) {
			throw new IllegalArgumentException("source and destination args are mandatory for restore");
		}
		File source = new File(cmd.getOptionValue("source"));
		if (!source.exists() || !source.isDirectory()) {
			throw new IllegalArgumentException("Backup source arg must be a valid directory");
		}

		File destination = new File(cmd.getOptionValue("destination"));
		if (destination.exists() && !destination.isDirectory()) {
			throw new IllegalArgumentException("Destination must be a folder");
		}

		List<String> registriesNames = FileUtils.listFiles(source, new String[] { "registry" }, false).stream()
				.map(File::getName).sorted(RegistryFilenameUtils.filenameComparator(false))
				.collect(Collectors.toList());

		UnzipIncrementalArchive uia = new UnzipIncrementalArchive(destination);
		for (String registry : registriesNames) {
			uia.unzip(new File(source, StringUtils.split(registry, ".")[0] + ".zip"));
		}
	}
}
