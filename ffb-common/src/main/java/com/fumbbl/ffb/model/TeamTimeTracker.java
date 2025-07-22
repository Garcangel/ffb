package com.fumbbl.ffb.model;

import java.util.*;

public class TeamTimeTracker {

	private final List<TimingEvent> timingLog = new ArrayList<>();
	private final List<TimingEvent> passiveLog = new ArrayList<>();
	private long totalTimeUsed = 0;
	private long totalTimeExceeded = 0;
	private long totalTimeUnused = 0;

	private long totalPassiveTimeUsed = 0;

	public static class TimingEvent {
		public final int half;
		public final int turnNumber;
		public final long durationMs;
		public final long turnLimitMs;
		public final long timestampMs;
		public final String reason; // string for reason: "regular", "touchdown", "half_end"

		public TimingEvent(int half, int turnNumber, long durationMs, long turnLimitMs, long timestampMs, String reason) {
			this.half = half;
			this.turnNumber = turnNumber;
			this.durationMs = durationMs;
			this.turnLimitMs = turnLimitMs;
			this.timestampMs = timestampMs;
			this.reason = reason;
		}

		@Override
		public String toString() {
			return "Half " + half +
				", Turn " + turnNumber +
				" = " + durationMs + "ms [" + reason + "]";
		}
	}

	public void recordTurn(int half, int turnNumber, long durationMs, long turnLimitMs, long timestampMs, String reason) {
		timingLog.add(new TimingEvent(half, turnNumber, durationMs, turnLimitMs, timestampMs, reason));
		totalTimeUsed += durationMs;
		if (turnLimitMs > 0) {
			if (durationMs > turnLimitMs) {
				totalTimeExceeded += (durationMs - turnLimitMs);
			} else {
				totalTimeUnused += (turnLimitMs - durationMs);
			}
		}
	}

	public void recordPassiveTime(long durationMs, String context) {
		if (durationMs <= 0) return;
		totalPassiveTimeUsed += durationMs;
		passiveLog.add(new TimingEvent(0, 0, durationMs, 0, System.currentTimeMillis(), context));
	}

	public long getTotalTimeUsed() { return totalTimeUsed; }
	public long getTotalTimeExceeded() { return totalTimeExceeded; }
	public long getTotalTimeUnused() { return totalTimeUnused; }
	public long getTotalPassiveTimeUsed() { return totalPassiveTimeUsed; }
	public List<TimingEvent> getTimingLog() { return timingLog; }
	public List<TimingEvent> getPassiveLog() { return passiveLog; }

	public double getAverageTurnTimeMs() {
		if (timingLog.isEmpty()) {
			return 0.0;
		}
		long sum = 0;
		for (TimingEvent timing : timingLog) {
			sum += timing.durationMs;
		}
		return ((double) sum) / timingLog.size();
	}
}

