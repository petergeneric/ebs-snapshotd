package com.github.petergeneric.aws.elasticsnap.daemon.service;

import com.amazonaws.services.ec2.model.Snapshot;
import com.github.petergeneric.aws.elasticsnap.daemon.type.DurableVolume;

public interface DiscardSnapshotService
{
	/**
	 * Discard a specific snapshot
	 * 
	 * @param volume
	 *            the volume holding the snapshot to discard
	 * @param snapshot
	 *            the snapshot to discard; if null then no snapshot will be discarded
	 */
	public void discard(DurableVolume volume, Snapshot snapshot);
}
