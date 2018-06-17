package fr.lespoulpes.backup.incremental;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Incremental {

	public static void main(String[] args) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		// create Options object
		Options options = new Options();

		// add option
		options.addOption("backup", false, "Do a backup");
		options.addOption("restore", false, "Do a restore");
		options.addOption("source", true, "Base source directory");
		options.addOption("destination", true, "Destination directory for the backup");
		options.addOption("hashAlgorithm", true, "Hash algorithm used to validate checksum");
		CommandLine cmd = parser.parse(options, args);
		
		if(!cmd.hasOption("backup") && !cmd.hasOption("restore")) {
			throw new IllegalArgumentException("Process should be either backup or restore");
		} else if(cmd.hasOption("backup") && cmd.hasOption("restore")) {
			throw new IllegalArgumentException("Process must be backup or restore. So provide only one");
		}
		
		IncrementalExecution execution = cmd.hasOption("backup") ? new Backup(cmd) : new Restore(cmd);
		execution.execute();
	}

}
