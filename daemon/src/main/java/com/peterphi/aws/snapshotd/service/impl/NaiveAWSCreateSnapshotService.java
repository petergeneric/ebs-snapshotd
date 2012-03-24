package com.peterphi.aws.snapshotd.service.impl;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.Volume;
import com.peterphi.aws.snapshotd.service.iface.CreateSnapshotService;
import com.peterphi.aws.snapshotd.service.iface.DiscardSnapshotService;
import com.peterphi.aws.snapshotd.type.DurableVolume;

/**
 * A Naive CreateSnapshotService which requests a snapshot without communicating to anyone to pause IO on the disk<br />
 * If used on live disks this may very well result in data corruption
 */
public class NaiveAWSCreateSnapshotService implements CreateSnapshotService
{
	private static final transient Logger log = Logger.getLogger(NaiveAWSCreateSnapshotService.class);

	protected AmazonEC2Client ec2;

	public NaiveAWSCreateSnapshotService()
	{
	}

	public NaiveAWSCreateSnapshotService(AmazonEC2Client ec2)
	{
		setEC2(ec2);
	}

	public void setEC2(AmazonEC2Client ec2)
	{
		this.ec2 = ec2;
	}

	@Override
	public Snapshot snapshot(DurableVolume volume)
	{
		if (volume == null)
			throw new IllegalArgumentException("No DurableVolume to snapshot!");

		final String description = getSnapshotDescription(volume.getVolume());

		final Snapshot snapshot = snapshot(volume.getVolume(), description);

		try
		{
			setTags(volume.getVolume().getTags(), snapshot);
		}
		catch (AmazonClientException e)
		{
			log.warn("Failed to set tags for " + snapshot.getSnapshotId() + ": " + e.getMessage(), e);
		}

		return snapshot;
	}

	private Snapshot snapshot(Volume volume, final String description)
	{
		if (volume == null)
			throw new IllegalArgumentException("No Volume to snapshot!");

		// Warn if someone snapshots an in-use volume with this snapshot service
		if (StringUtils.equalsIgnoreCase(volume.getState(), "in-use"))
		{
			log.warn("Creating snapshot of in-use EBS Volume " + volume.getVolumeId()
					+ ": snapshot may be in inconsistent state!");
		}

		CreateSnapshotRequest req = new CreateSnapshotRequest(volume.getVolumeId(), description);

		CreateSnapshotResult result = ec2.createSnapshot(req);

		return result.getSnapshot();
	}

	protected String getSnapshotDescription(Volume volume)
	{
		return "Snapshot of " + volume.getVolumeId();
	}

	/**
	 * Set the tags for a Snapshot
	 * 
	 * @param tags
	 * @param to
	 */
	protected void setTags(List<Tag> tags, Snapshot to)
	{
		if (tags.isEmpty())
			return; // no action to take

		CreateTagsRequest req = new CreateTagsRequest(Collections.singletonList(to.getSnapshotId()), tags);

		ec2.createTags(req);
	}
}
