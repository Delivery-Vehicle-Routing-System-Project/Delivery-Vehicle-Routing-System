package DVRS;

public class IntegerList {

	IntegerList() {
		data = null;
		used = 0;
		allocation = 0;
	}
	

	IntegerList(IntegerList src) {
		used = src.used;
		allocation = src.allocation;
		if (src.data == null) {
			data = null;
		} else {
			data = new int[allocation];
			for (int i=0; i<used; i++) {
				data[i] = src.data[i];
			}
		}
	}
	
	public void add(int newValue) {
		
		// Make sure enough memory is allocated
		if (used >= allocation) {
			
			// If false then there is a bug somewhere
			assert used == allocation;

			// Make an initial guess of the new size for the array
			int newAllocationSize = (allocation < minAllocation) ? minAllocation : allocation * 2;
			
			// Resize memory allocation
			resizeAllocation(newAllocationSize);
		}

		// Add value to list
		data[used++] = newValue;
	}

	public void remove(int index) {
		assert (0 <= index) && (index < used);
		for (int i=used-1; i>index; i--) {
			data[i - 1] = data[i];
		}
		used--;
	}

	public void removeUnordered(int index) {
		assert (0 <= index) && (index < used);
		if (index < --used) {
			data[index] = data[used];
		}
	}
	
	public void push(int newValue) {
		add(newValue);
	}

	public int pop() {
		assert used > 0;
		return data[--used];
	}
	
	public void set(int index, int newValue) {
		assert index < used;
		data[index] = newValue;
	}
	
	public int get(int index) {
		assert index < used;
		return data[index];
	}
	
	public int find(int value) {
		for (int i=0; i<used; i++) {
			if (data[i] == value) return i;
		}
		return -1;
	}

	public int find(int value, int startIndex) {
		for (int i=startIndex; i<used; i++) {
			if (data[i] == value) return i;
		}
		return -1;
	}
	
	public boolean isEqual(IntegerList rhs) {
		if (used != rhs.used) return false;
		for (int i=0; i<used; i++) {
			if (data[i] != rhs.data[i]) return false;
		}
		return true;
	}
	
	public int size() {
		return used;
	}
	
	public boolean isEmpty() {
		return used == 0;
	}
	
	public void clear() {
		used = 0;
	}
	
	public void resize(int newSize) {
		assert newSize >= 0;
		if (newSize > allocation) resizeAllocation(newSize);
		used = newSize;
	}

	public int capacity() {
		return allocation;
	}
	
	public void reserve(int requestedTotalCapacity) {
		assert requestedTotalCapacity >= 0;
		if (allocation < requestedTotalCapacity) {
			resizeAllocation(requestedTotalCapacity);
		}
	}
	
	public void shrinkToFit() {
		resizeAllocation(used);
	}
	
	private void resizeAllocation(int newSize) {
		
		// Check if allocating or deleting memory
		int[] temp = null;
		if (newSize > 0) {
		
			// Allocate new array
			temp = new int[newSize];
	
			// Copy values from previous array
			for (int i=0; i<used; i++) {
				temp[i] = data[i];
			}
		}
		
		// Save new data
		data = temp;
		allocation = newSize;
	}

	@Override
	public String toString() {
		String t = "";
		if (used > 0) {
			t = Integer.toString(data[0]);
			for (int i=1; i<used; i++) t += ","+Integer.toString(data[i]);
		}
		return "["+t+"]";
	}
	
	private int[] data;
	private int used;
	private int allocation;
	private static final int minAllocation = 8;
}
