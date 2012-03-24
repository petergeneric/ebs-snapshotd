package com.github.petergeneric.aws.elasticsnap.daemon.util;

import java.util.Comparator;

import com.amazonaws.services.ec2.model.Snapshot;

/**
 * A comparator that places EBS Snapshots in ascending creation date order
 */
public class SnapshotAgeComparator implements Comparator<Snapshot>
{
	@Override
	public int compare(Snapshot a, Snapshot b)
	{
		if (a.getStartTime() == null)
			throw new IllegalArgumentException("Snapshot has no start time: " + a);
		if (b.getStartTime() == null)
			throw new IllegalArgumentException("Snapshot has no start time: " + b);

		return a.getStartTime().compareTo(b.getStartTime());
	}
}
