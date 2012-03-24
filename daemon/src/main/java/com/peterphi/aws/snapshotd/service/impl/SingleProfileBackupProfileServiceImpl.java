package com.peterphi.aws.snapshotd.service.impl;

import com.peterphi.aws.snapshotd.service.iface.BackupProfileService;
import com.peterphi.aws.snapshotd.type.BackupProfile;
import com.peterphi.aws.snapshotd.type.DurableVolume;
import com.peterphi.aws.snapshotd.util.AWSTagHelper;

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
