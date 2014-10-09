package com.pmh.wlmq.https;

import lombok.extern.log4j.Log4j;

@Log4j
public class BookTask implements Runnable {

	private long time;
	private BookDispatcher bookDispatcher;

	public BookTask(long time, BookDispatcher bookDispatcher) {
		this.time = time;
		this.bookDispatcher = bookDispatcher;
	}

	public void run() {
		TrainBook task = new TrainBook(time, this.bookDispatcher);
		try {
			task.book();
		} catch (Exception e) {
			log.error("task occurred exception", e);
		}
	}

}
