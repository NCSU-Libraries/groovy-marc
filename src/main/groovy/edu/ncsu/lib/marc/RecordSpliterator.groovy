package edu.ncsu.lib.marc
import org.marc4j.marc.Record

import java.util.function.Consumer

/**
 * Spliterator implementation to let MarcStreamReader act as a Java 8 iterable.
 **/
class RecordSpliterator implements Spliterator<Record> {
	
	private Reader reader;
	
	public RecordSpliterator(Reader reader) {
		this.reader = reader;
	}

	@Override
	public boolean tryAdvance(Consumer<? super Record> action) {
		Objects.requireNotNull(action)
		if ( reader.hasNext() ) {
			action.accept( reader.next() ) 
			return true;
		}
		return false
	}

	@Override
	public void forEachRemaining(Consumer<? super Record> action) {
		Objects.requireNonNull(action)
		while( reader.hasNext() ) {
			action.accept(reader.next())
		}
	}

	@Override
	public Spliterator<Record> trySplit() {
		return null;
	}

	@Override
	public long estimateSize() {
		return Long.MAX_VALUE;
	}

	@Override
	public long getExactSizeIfKnown() {
		return -1;
	}

	@Override
	public int characteristics() {
		return Spliterator.IMMUTABLE | Spliterator.NONNULL;
	}

	@Override
	public boolean hasCharacteristics(int characteristics) {
		return characteristics & characteristics()
	}

	@Override
	public Comparator<? super Record> getComparator() {
		return null;
	}

}
