package com.peterphi.aws.snapshotd.service.iface;

import com.amazonaws.services.ec2.model.Snapshot;
import com.peterphi.aws.snapshotd.type.DurableVolume;

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
