package pgenopt.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayMaker<T> {
	
	private Class<T> type;
	
	public ArrayMaker(Class<T> type) {
		this.type = type;
	}
	
	@SuppressWarnings("unchecked")
	public T[] createArray(int size) {
		return (T[]) Array.newInstance(type, size);
	}
	
	public List<T> createList() {
		return new ArrayList<T>();
	}

}
