package com.peterphi.aws.snapshotd.service.iface;

/**
 * Responsible for orchestrating various other services to enact a backup run
 */
public interface BackupService
{
	/**
	 * Performs a single run of backup tasks
	 */
	public void backup();

	public void setDurableVolumeDiscoveryService(DurableVolumeDiscoveryService discoverer);

	public void setProfileService(BackupProfileService profiler);

	public void setVolumeActionService(VolumeActionService actioner);

	public void setNotificationService(NotificationService notifier);
}
