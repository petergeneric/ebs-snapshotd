package com.peterphi.aws.snapshotd.type;

import java.util.*;

import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.ec2.model.Volume;
import com.peterphi.aws.snapshotd.util.SnapshotAgeComparator;

public class DurableVolume
{
	/**
	 * The backup profile for this volume/snapshot set
	 */
	protected BackupProfile profile;

	/**
	 * The live volume (if one exists)
	 */
	protected Volume volume;

	/**
	 * All known snapshots for this volume (may be empty, may not contain null)
	 */
	protected final List<Snapshot> snapshots = new ArrayList<Snapshot>();

	public Volume getVolume()
	{
		return volume;
	}

	public void setVolume(Volume volume)
	{
		this.volume = volume;
	}

	/**
	 * Returns a read-only list of the Snapshots for this volume<br />
	 * If none were provided then an empty list is returned
	 * 
	 * @return
	 */
	public List<Snapshot> getSnapshots()
	{
		return snapshots;
	}

	/**
	 * Re-sort the Snapshots in the <code>snapshots</code> list by their age, youngest first, using {@link SnapshotAgeComparator}
	 * 
	 */
	public void sortSnapshots()
	{
		Collections.sort(this.snapshots, new SnapshotAgeComparator());
	}

	public BackupProfile getBackupProfile()
	{
		return profile;
	}

	public void setBackupProfile(BackupProfile profile)
	{
		this.profile = profile;
	}

	@Override
	public String toString()
	{
		List<String> ids = new ArrayList<String>(snapshots.size() + 1);

		ids.add(getVolumeId());

		for (Snapshot snapshot : snapshots)
		{
			ids.add(snapshot.getSnapshotId());
		}

		return "DurableVolume [profile=" + profile + ", ids=" + ids + "]";
	}

	protected String getVolumeId()
	{
		if (volume != null)
		{
			return volume.getVolumeId();
		}
		else
		{
			for (Snapshot snapshot : snapshots)
			{
				return snapshot.getVolumeId();
			}

			throw new IllegalArgumentException("Could not retrieve volume id!");
		}
	}
}
