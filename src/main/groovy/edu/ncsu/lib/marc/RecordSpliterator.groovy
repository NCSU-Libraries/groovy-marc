package edu.ncsu.lib.marc
/*

     Copyright (C) 2015 North Carolina State University

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import org.marc4j.marc.Record

import java.util.function.Consumer

/**
 * Spliterator implementation to let MarcStreamReader act as a Java 8 iterable.
 * JDK 8+ only.
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
