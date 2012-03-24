package com.github.petergeneric.aws.elasticsnap.daemon.service;

import com.amazonaws.services.ec2.model.Snapshot;
import com.github.petergeneric.aws.elasticsnap.daemon.type.DurableVolume;

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
