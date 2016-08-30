package novaz.core;


import java.util.*;

/**
 * Created on 30-8-2016
 */
public class ConfigurationProperties extends Properties {
	private static final long serialVersionUID = 1L;

	public Enumeration<Object> keys() {
		Enumeration<Object> keysEnum = super.keys();
		Vector<Object> keyList = new Vector<>();

		while (keysEnum.hasMoreElements()) {
			keyList.add(keysEnum.nextElement());
		}

		Collections.sort(keyList, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		return keyList.elements();
	}
}