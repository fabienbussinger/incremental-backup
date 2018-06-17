package fr.lespoulpes.backup.incremental.file.seeking;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.lespoulpes.backup.incremental.tree.Node;
import fr.lespoulpes.backup.incremental.tree.NodeBuilder;

public class FileSeekerFJP extends AbstractFileSeeker {
	private final File sourceDirectory;
	private final NodeBuilder nodeBuilder;

	public FileSeekerFJP(File sourceDirectory, String hasher) {
		this.nodeBuilder = new NodeBuilder(hasher);
		File mayBeDir = sourceDirectory;
		if (!mayBeDir.exists() || !mayBeDir.isDirectory()) {
			throw new RuntimeException("Cannot operate on regular file as source");
		}
		this.sourceDirectory = mayBeDir;

	}

	private static class SeekTask extends RecursiveTask<Node> {
		private static final Logger LOG = LogManager.getLogger(SeekTask.class);
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final AtomicLong fileCounter;
		private final File file;
		private final NodeBuilder nodeBuilder;

		public SeekTask(AtomicLong fileCounter, NodeBuilder nodeBuilder, File file) {
			this.nodeBuilder = nodeBuilder;
			this.file = file;
			this.fileCounter = fileCounter;
		}

		@Override
		protected Node compute() {
			LOG.trace("Start compute on node {}", () -> this.file.getAbsolutePath());
			final Node current = this.nodeBuilder.create(this.file);
			this.fileCounter.incrementAndGet();
			File[] files = this.file.listFiles();
			if (files == null) {
				LOG.trace("Node {} is a regular file, returning...", () -> this.file.getAbsolutePath());
				return current;
			} else {
				final List<SeekTask> tasks = new ArrayList<>();

				for (File child : files) {
					SeekTask task = new SeekTask(this.fileCounter, nodeBuilder, child);
					task.fork();
					tasks.add(task);
				}

				for (final SeekTask task : tasks) {
					current.getChildren().add(task.join());
				}
				return current;
			}
		}

	}

	@Override
	protected Node startReading() {
		final ForkJoinPool fjp = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 3);
		try {
			return fjp.invoke(new SeekTask(this.getFileCounter(), this.nodeBuilder, this.sourceDirectory));
		} finally {
			fjp.shutdown();
		}
	}
}
