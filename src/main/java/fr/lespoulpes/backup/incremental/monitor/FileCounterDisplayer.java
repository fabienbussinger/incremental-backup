package fr.lespoulpes.backup.incremental.monitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.lespoulpes.backup.incremental.file.seeking.FileSeeker;

public class FileCounterDisplayer extends Thread {
	private static final Logger LOG = LogManager.getLogger(FileCounterDisplayer.class);

	private boolean stop;
	private final FileSeeker fs;

	public FileCounterDisplayer(FileSeeker fs) {
		this.fs = fs;
	}

	public void stopIt() {
		this.stop = true;
		this.interrupt();
	}

	@Override
	public void run() {
		final long start = System.currentTimeMillis();
		while (!stop) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				if (!stop) {
					throw new RuntimeException("Bad exception while sleeping my own thread", e);
				}
			}
			doTrace(start);
		}

		doTrace(start);
	}

	private void doTrace(final long start) {
		if (!stop) {
			long nbFiles = this.fs.getFileCounter().get();
			long now = System.currentTimeMillis();
			LOG.info("Read {} files in {} ms - rate : {} files/s", nbFiles, now - start,
					(long) nbFiles / ((now - start) / 1000));
		}
	}

}