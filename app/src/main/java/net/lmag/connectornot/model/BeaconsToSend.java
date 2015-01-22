package net.lmag.connectornot.model;

import java.util.LinkedList;
import java.util.List;

public class BeaconsToSend {
	
	private static List<Integer> sList = new LinkedList<Integer>();
	
	private static Object sLock = new Object();
	
	public static void add(Integer ev) {
		synchronized(sLock) {
			sList.add(ev);
		}
	}
	
	public static void addAll(LinkedList<Integer> evs) {
		synchronized(sLock) {
			sList.addAll(evs);
		}
	}
	
	public static List<Integer> getAll() {
		List<Integer> ret = null;
		LinkedList<Integer> temp = new LinkedList<Integer>();
		synchronized (sLock) {
			ret = sList;
			sList = temp;
		}
		return ret;
	}
	
//	private static ConcurrentHashMap<String, List<BeaconEvent>> sBeaconMap =
//			new ConcurrentHashMap<String, List<BeaconEvent>> (20, (float)0.75, 8);
//	
//	public static void add(String pluginName, BeaconEvent ev) {
//		sBeaconMap.putIfAbsent(pluginName, new LinkedList<BeaconEvent>());
//
//		
//		sBeaconMap.get(pluginName).add(ev);
//		
//	}
//	
//	public static List<BeaconEvent> getEvents() {
//		
//	}
	
//	private static final int CAPACITY = 100;
//	
//	private static BlockingQueue<BeaconEvent> sQueue = new LinkedBlockingQueue<BeaconEvent>(CAPACITY);
//	
//	public static boolean offer(BeaconEvent ev) {
//		return sQueue.offer(ev);
//	}
//	
//	public static List<BeaconEvent> consumeAll() {
//		sQueue.
//	}
	 

}
