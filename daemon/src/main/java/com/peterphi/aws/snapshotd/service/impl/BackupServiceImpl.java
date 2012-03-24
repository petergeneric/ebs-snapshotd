package com.peterphi.aws.snapshotd.service.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.peterphi.aws.snapshotd.service.iface.BackupProfileService;
import com.peterphi.aws.snapshotd.service.iface.BackupService;
import com.peterphi.aws.snapshotd.service.iface.DurableVolumeDiscoveryService;
import com.peterphi.aws.snapshotd.service.iface.NotificationService;
import com.peterphi.aws.snapshotd.service.iface.VolumeActionService;
import com.peterphi.aws.snapshotd.type.DurableVolume;

public class BackupServiceImpl implements BackupService
{
	private static final transient Logger log = Logger.getLogger(BackupServiceImpl.class);

	protected DurableVolumeDiscoveryService discoverer;
	protected BackupProfileService profiler;
	protected VolumeActionService actioner;
	protected NotificationService notifier;

	@Override
	public void backup()
	{
		// Find all volumes we could process
		final List<DurableVolume> volumes = discoverer.discover();

		// TODO we could use a threadpool here to improve performance for larger datasets
		for (DurableVolume volume : volumes)
		{
			backup(volume);
		}
	}

	/**
	 * Perform any necessary action for a volume, logging any problems
	 * 
	 * @param volume
	 */
	protected void backup(final DurableVolume volume)
	{
		// Only process this volume if we are able to determine its profile
		if (assignProfile(volume))
		{
			try
			{
				actioner.handle(volume);
			}
			catch (Exception e)
			{
				log.error("Error processing volume " + volume + ". Error: " + e.getMessage(), e);

				notifier.error(volume, "Error during handling of volume: " + e.getMessage());
			}
		}
	}

	/**
	 * Assigns a profile (if possible), logging any failure or rejection<br />
	 * 
	 * @param volume
	 * @return true if and only if the volume has a Backup Profile
	 */
	protected boolean assignProfile(final DurableVolume volume)
	{
		try
		{
			profiler.assignProfile(volume);

			if (volume.getBackupProfile() == null)
				throw new IllegalStateException("Profiler did not throw an exception but failed to assign backup profile!");
			else
				return true;
		}
		catch (Exception e)
		{
			log.info("BackupProfileService rejected volume: " + volume + ". Reason: " + e.getMessage());
			return false;
		}
	}

	// setters
	@Override
	public void setDiscoverer(DurableVolumeDiscoveryService discoverer)
	{
		this.discoverer = discoverer;
	}

	@Override
	public void setProfiler(BackupProfileService profiler)
	{
		this.profiler = profiler;
	}

	@Override
	public void setActioner(VolumeActionService actioner)
	{
		this.actioner = actioner;
	}

	@Override
	public void setNotifier(NotificationService notifier)
	{
		this.notifier = notifier;
	}

}
