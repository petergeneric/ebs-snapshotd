package com.peterphi.aws.snapshotd.service;

import com.amazonaws.services.ec2.model.Snapshot;
import com.peterphi.aws.snapshotd.type.DurableVolume;

public interface CreateSnapshotService
{
	/**
	 * Start the creation of an EBS Snapshot, blocking until the Snapshot has been created
	 * 
	 * @param volume
	 *            the DurableVolume to back up
	 * @return
	 */
	public Snapshot snapshot(DurableVolume volume);
}
