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

	public void setDiscoverer(DurableVolumeDiscoveryService discoverer);

	public void setProfiler(BackupProfileService profiler);

	public void setActioner(VolumeActionService actioner);

	public void setNotifier(NotificationService notifier);
}
