package com.peterphi.aws.snapshotd.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.ec2.model.Volume;
import com.peterphi.aws.snapshotd.service.iface.BackupProfileService;
import com.peterphi.aws.snapshotd.service.iface.CreateSnapshotService;
import com.peterphi.aws.snapshotd.service.iface.DiscardSnapshotService;
import com.peterphi.aws.snapshotd.service.iface.NotificationService;
import com.peterphi.aws.snapshotd.service.iface.VolumeActionService;
import com.peterphi.aws.snapshotd.type.BackupProfile;
import com.peterphi.aws.snapshotd.type.DurableVolume;

/**
 * An implementation of the VolumeActionService which takes a single action (based on the BackupProfile)<br />
 * It is very conservative, not taking any discard actions if there are any issues with the existing snapshots
 */
public class VolumeActionServiceImpl implements VolumeActionService
{
	private static final transient Logger log = Logger.getLogger(VolumeActionServiceImpl.class);

	protected NotificationService notify;

	protected CreateSnapshotService snapshotter;
	protected DiscardSnapshotService discarder;

	public VolumeActionServiceImpl()
	{
	}

	public void setNotificationService(NotificationService notify)
	{
		this.notify = notify;
	}

	public void setCreateSnapshotService(CreateSnapshotService snapshotter)
	{
		this.snapshotter = snapshotter;
	}

	public void setDiscardSnapshotService(DiscardSnapshotService discarder)
	{
		this.discarder = discarder;
	}

	@Override
	public void handle(final DurableVolume volume)
	{
		final BackupProfile profile = volume.getBackupProfile();

		if (profile == null)
			throw new IllegalArgumentException("DurableVolume has no backup profile: will not evaluate");

		if (shouldDiscard(volume))
		{
			final Snapshot oldest = getOldest(volume.getSnapshots());

			// Request the snapshot be discarded
			discarder.discard(volume, oldest);

			notify.discarded(volume, oldest);
		}
		else if (shouldSnapshot(volume))
		{
			// Request the snapshot be created
			final Snapshot snapshot = snapshotter.snapshot(volume);

			notify.created(volume, snapshot);
		}
		else
		{
			log.trace("No action necessary for this turn: " + volume);
		}
	}

	/**
	 * Decides if we should create a snapshot. The decision is made as follows:
	 * <ol>
	 * <li>Are we allowed to perform an automatic backup?</li>
	 * <li>Is there an EBS volume to snapshot?</li>
	 * <li>Is the EBS volume not being deleted?</li>
	 * <li>If there are existing snapshots, is the youngest older than the profile's backup frequency?</li>
	 * <li>If there are no snapshots, we create a snapshot</li>
	 * <ol>
	 * 
	 * @param volume
	 * @return
	 */
	private boolean shouldSnapshot(DurableVolume volume)
	{
		final BackupProfile profile = volume.getBackupProfile();

		if (profile.getFrequency() == 0)
		{
			log.debug("Decided not to snapshot " + volume + ": profile does not permit automatic backup");
			return false;
		}

		final Snapshot youngest = getYoungest(volume.getSnapshots());

		if (volume.getVolume() == null)
		{
			log.debug("Decided not to snapshot " + volume + ": no EBS volume to snapshot");
			return false;
		}
		else if (isEBSDeleting(volume))
		{
			log.debug("Decided not to snapshot " + volume.getVolume().getVolumeId() + ": EBS Volume is deleting");
			return false;
		}

		if (youngest != null)
		{
			final long age = getAge(youngest);

			if (age >= profile.getFrequency())
			{
				log.debug("Decided to snapshot " + volume + ": requisite backup frequency has elapsed");
				return true;
			}
			else
			{
				log.debug("Decided not to snapshot " + volume.getVolume().getVolumeId() + ": last backup still too young");
			}
		}
		else
		{
			log.debug("Decided to snapshot " + volume.getVolume().getVolumeId() + ": no snapshots exist");
			return true;
		}

		return false;
	}

	/**
	 * Decides if we should discard the oldest snapshot for a volume.The decision is made as follows:
	 * <ol>
	 * <li>Are we permitted to discard snapshots automatically?</li>
	 * <li>Is the EBS volume (if one exists) in a non-error state?</li>
	 * <li>Is the EBS volume (if one exists) in a stable state (EBS Volume not being deleted, EBS snapshots not being created)?</li>
	 * <li>Are there any snapshots to discard?</li>
	 * <li>Are the snapshots all in a stable state (non-error non-pending)?</li>
	 * <li>Do we have more than the minimum number of snapshots?</li>
	 * <li>Is the oldest snapshot older than the profile's expire frequency?</li>
	 * </ol>
	 * 
	 * @param volume
	 * @return
	 */
	protected boolean shouldDiscard(DurableVolume volume)
	{
		if (volume.getBackupProfile().getExpire() == 0)
		{
			log.debug("Decided not to discard " + volume + ": profile does not permit automatic discard");
			return false;
		}
		else if (isUnhealthy(volume))
		{
			log.debug("Decided not to consider for discard: contents not completely healthy " + volume);
			return false;
		}
		else if (isInFlux(volume))
		{
			log.debug("Decided not to consider for discard: contents are in flux " + volume);
			return false;
		}
		else
		{
			return shouldDiscard(volume.getBackupProfile(), volume.getSnapshots());
		}
	}

	/**
	 * Decide whether we should discard the oldest snapshot in <code>snapshots</code>
	 * 
	 * @param profile
	 *            the backup profile
	 * @param snapshots
	 *            the available snapshots
	 * @return
	 */
	protected boolean shouldDiscard(BackupProfile profile, List<Snapshot> snapshots)
	{
		if (profile == null)
			throw new IllegalArgumentException("No BackupProfile supplied!");
		if (snapshots == null)
			throw new IllegalArgumentException("Snapshot list is null!");

		final int count = snapshots.size();

		if (count == 0)
		{
			return false; // there were no snapshots in the first place
		}

		final Snapshot oldest = getOldest(snapshots);

		if (count > profile.getMinimum())
		{
			final long age = getAge(oldest);

			if (age >= profile.getExpire())
			{
				log.info("Decided to discard snapshot " + oldest.getSnapshotId()
						+ ": more than the minimum snapshots and this snapshot is old enough to be expired");

				return true;
			}
			else
			{
				log.debug("Decided not to expire " + oldest.getSnapshotId() + ": it is not old enough to be expired");
			}
		}
		else
		{
			log.debug("Decided not to expire " + oldest.getSnapshotId() + ": fewer than the minimum number of snapshots");
		}

		return false; // don't discard
	}

	//
	// Simple helper functions
	//

	protected long now()
	{
		return System.currentTimeMillis();
	}

	protected Snapshot getOldest(List<Snapshot> snapshots)
	{
		if (snapshots.isEmpty())
			return null;
		else
			return snapshots.get(0); // Retrieve the oldest snapshot in the sorted list
	}

	protected Snapshot getYoungest(List<Snapshot> snapshots)
	{
		if (snapshots.isEmpty())
			return null;
		else
		{
			final int last = snapshots.size() - 1;

			return snapshots.get(last);
		}
	}

	protected long getAge(Snapshot snapshot)
	{
		return now() - snapshot.getStartTime().getTime();
	}

	/**
	 * Determines if any of the contents of a DurableVolume are in an unhealthy state (EBS Volume/Snapshots in error)
	 * 
	 * @param volume
	 * @return
	 */
	protected boolean isUnhealthy(DurableVolume volume)
	{
		if (isEBSError(volume))
		{
			log.trace("EBS Volume in error: " + volume.getVolume().getVolumeId());
			return false;
		}

		for (Snapshot snapshot : volume.getSnapshots())
		{
			if (StringUtils.equalsIgnoreCase(snapshot.getState(), "error"))
			{
				log.trace("EBS snapshot is in error: " + snapshot.getSnapshotId());

				return true;
			}
		}

		return false;
	}

	/**
	 * Determines if any of the contents of a DurableVolume are in a state of change (EBS Volume deleting or EBS Snapshot creating)
	 * 
	 * @param volume
	 * @return
	 */
	protected boolean isInFlux(DurableVolume volume)
	{
		if (isEBSDeleting(volume))
		{
			log.trace("EBS Volume in deleting: " + volume.getVolume().getVolumeId());
			return true;
		}

		for (Snapshot snapshot : volume.getSnapshots())
		{
			if (StringUtils.equalsIgnoreCase(snapshot.getState(), "pending"))
			{
				log.trace("Volume in flux: EBS snapshot is pending: " + snapshot.getSnapshotId());

				return true;
			}
		}

		return false;
	}

	/**
	 * Determines if there is an EBS Volume associated with the DurableVolume, and if so whether the EBS Volume is in the "deleting" state
	 * 
	 * @param volume
	 * @return
	 */
	protected boolean isEBSDeleting(DurableVolume volume)
	{
		final Volume ebs = volume.getVolume();

		if (ebs != null)
		{
			return StringUtils.equalsIgnoreCase(ebs.getState(), "deleting");
		}
		else
		{
			return false;
		}
	}

	/**
	 * Determines if there is an EBS Volume associated with the DurableVolume, and if so whether the EBS Volume is in the "error" state
	 * 
	 * @param volume
	 * @return
	 */
	private boolean isEBSError(DurableVolume volume)
	{
		final Volume ebs = volume.getVolume();

		if (ebs != null)
		{
			return StringUtils.equalsIgnoreCase(ebs.getState(), "error");
		}
		else
		{
			return false;
		}
	}
}
