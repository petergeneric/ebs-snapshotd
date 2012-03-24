package com.github.petergeneric.aws.elasticsnap.daemon.service.impl;

import com.github.petergeneric.aws.elasticsnap.daemon.service.BackupProfileService;
import com.github.petergeneric.aws.elasticsnap.daemon.type.BackupProfile;
import com.github.petergeneric.aws.elasticsnap.daemon.type.DurableVolume;

/**
 * A simple BackupProfileService implementation that takes a single BackupProfile to apply to all DurableVolumes
 */
public class SingleProfileBackupProfileServiceImpl implements BackupProfileService
{
	protected BackupProfile profile;

	public void setProfile(BackupProfile profile)
	{
		if (profile == null)
			throw new IllegalArgumentException("Must provide a backup profile!");

		this.profile = profile;
	}

	@Override
	public void assignProfile(DurableVolume volume)
	{
		if (profile == null)
			throw new IllegalArgumentException("No profile to assign!");

		volume.setBackupProfile(profile);
	}
}
