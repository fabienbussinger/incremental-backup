package fr.lespoulpes.backup.incremental.file.seeking;

import java.util.concurrent.atomic.AtomicLong;

import fr.lespoulpes.backup.incremental.monitor.FileCounterDisplayer;
import fr.lespoulpes.backup.incremental.tree.Node;

public abstract class AbstractFileSeeker implements FileSeeker {
	private final AtomicLong fileCounter = new AtomicLong();

	public AbstractFileSeeker() {
	}

	@Override
	public final Node read() {
		FileCounterDisplayer fileCounterDisplayer = new FileCounterDisplayer(this);
		fileCounterDisplayer.start();
		Node result = startReading();
		fileCounterDisplayer.stopIt();
		
		return result;
	}

	@Override
	public AtomicLong getFileCounter() {
		return this.fileCounter;
	}

	protected abstract Node startReading();
}
