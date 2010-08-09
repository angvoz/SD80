/*
 * Templates.h
 *
 *  Created on: Jan 28, 2010
 *      Author: eswartz
 */

#ifndef TEMPLATES_H_
#define TEMPLATES_H_

template <class T>
class List {
public:
	List() : m_data(0), m_length(0), m_max(0)
	{
	}
	int length() {
		return m_length;
	}
	void add(T item);
	const T& operator[](unsigned int index) { return m_data[index]; }
	
private:
	T* m_data;
	int m_length;
	int m_max;
};

template <class T>
void List<T>::add(T item) {
	if (m_length >= m_max) {
		int newmax = m_max * 2;
		if (newmax == 0)
			newmax = 4;
		
		T* copy = new T[newmax];
		for (int i = 0; i < m_length; i++)
			copy[i] = m_data[i];
		delete[] m_data;
		
		m_data = copy;
		m_length++;
		m_max = newmax;
	}
}

extern void makelist();

#endif /* TEMPLATES_H_ */
