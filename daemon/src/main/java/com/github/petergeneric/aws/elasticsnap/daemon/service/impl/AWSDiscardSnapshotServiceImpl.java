package com.github.petergeneric.aws.elasticsnap.daemon.service.impl;

import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.Snapshot;
import com.github.petergeneric.aws.elasticsnap.daemon.service.DiscardSnapshotService;
import com.github.petergeneric.aws.elasticsnap.daemon.type.DurableVolume;

public class AWSDiscardSnapshotServiceImpl implements DiscardSnapshotService
{
	private static final transient Logger log = Logger.getLogger(AWSDiscardSnapshotServiceImpl.class);

	protected AmazonEC2Client ec2;
	protected boolean enabled = true;

	public AWSDiscardSnapshotServiceImpl()
	{
	}

	public AWSDiscardSnapshotServiceImpl(AmazonEC2Client ec2)
	{
		setEC2(ec2);
	}

	public void setEC2(AmazonEC2Client ec2)
	{
		this.ec2 = ec2;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	@Override
	public void discard(DurableVolume volume, Snapshot snapshot)
	{
		if (snapshot == null)
			throw new IllegalArgumentException("No snapshot to discard");

		DeleteSnapshotRequest req = new DeleteSnapshotRequest(snapshot.getSnapshotId());

		if (enabled)
		{
			ec2.deleteSnapshot(req);
		}
		else
		{
			log.warn("Suppressing EC2 discard call: " + req);
		}
	}
}
