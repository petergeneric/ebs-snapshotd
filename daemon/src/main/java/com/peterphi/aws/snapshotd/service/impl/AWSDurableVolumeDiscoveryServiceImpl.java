package com.peterphi.aws.snapshotd.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsResult;
import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.ec2.model.Volume;
import com.peterphi.aws.snapshotd.service.iface.DurableVolumeDiscoveryService;
import com.peterphi.aws.snapshotd.type.DurableVolume;
import com.peterphi.aws.snapshotd.util.SnapshotAgeComparator;

public class AWSDurableVolumeDiscoveryServiceImpl implements DurableVolumeDiscoveryService
{
	private static final transient Logger log = Logger.getLogger(NaiveAWSCreateSnapshotService.class);

	protected AmazonEC2Client ec2;

	public AWSDurableVolumeDiscoveryServiceImpl()
	{
	}

	public AWSDurableVolumeDiscoveryServiceImpl(AmazonEC2Client ec2)
	{
		setEC2(ec2);
	}

	public void setEC2(AmazonEC2Client ec2)
	{
		this.ec2 = ec2;
	}

	@Override
	public List<DurableVolume> discover()
	{
		final List<Volume> volumes = getVolumes();
		final List<Snapshot> snapshots = getSnapshots();

		List<DurableVolume> combined = createDurableVolumes(volumes, snapshots);

		return combined;
	}

	protected List<Snapshot> getSnapshots()
	{
		return ec2.describeSnapshots(new DescribeSnapshotsRequest().withOwnerIds("self")).getSnapshots();
	}

	protected List<Volume> getVolumes()
	{
		return ec2.describeVolumes().getVolumes();
	}

	protected List<DurableVolume> createDurableVolumes(List<Volume> volumes, List<Snapshot> snapshots)
	{
		final Map<String, DurableVolume> durable = new HashMap<String, DurableVolume>(volumes.size());

		// First, construct DurableVolume entries for all Volumes we know about
		createForVolumes(volumes, durable);

		// Now populate those DurableVolume entries with all Snapshots we know about; furthermore, create and populate any unknown DurableVolumes
		populateAndCreateForSnapshots(snapshots, durable);

		return new ArrayList<DurableVolume>(durable.values());
	}

	/**
	 * Creates the DurableVolumes based on a list of EBS Volumes
	 * 
	 * @param volumes
	 * @param durable
	 */
	private void createForVolumes(List<Volume> volumes, final Map<String, DurableVolume> durable)
	{
		for (Volume volume : volumes)
		{
			final String volumeId = volume.getVolumeId();

			if (!durable.containsKey(volumeId))
			{
				DurableVolume durableVolume = new DurableVolume();
				durableVolume.setVolume(volume);

				durable.put(volumeId, durableVolume);
			}
			else
			{
				log.warn("API returned Volumes with duplicate ids: " + volumes);
			}
		}
	}

	/**
	 * Populates (and creates if necessary) DurableVolumes based on snapshots
	 * 
	 * @param snapshots
	 * @param durable
	 */
	private void populateAndCreateForSnapshots(List<Snapshot> snapshots, final Map<String, DurableVolume> durable)
	{
		// Sort all the snapshots by age (so when iterating over the list we'll insert them into the DurableVolume in order)
		Collections.sort(snapshots, new SnapshotAgeComparator());

		for (Snapshot snapshot : snapshots)
		{
			final String volumeId = snapshot.getVolumeId();

			if (volumeId != null)
			{
				DurableVolume durableVolume = durable.get(volumeId);

				// Create the DurableVolume if necessary
				if (!durable.containsKey(volumeId))
				{
					durableVolume = new DurableVolume();
					durable.put(volumeId, durableVolume);
				}

				// Now add the snapshot
				durableVolume.getSnapshots().add(snapshot);
			}
			else
			{
				log.debug("Ignoring snapshot " + snapshot.getSnapshotId() + ": it has no volumeId (are you using Eucalyptus?)");
			}
		}
	}
}
