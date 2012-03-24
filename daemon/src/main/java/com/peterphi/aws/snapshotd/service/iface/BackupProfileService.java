package com.peterphi.aws.snapshotd.service.iface;

import com.peterphi.aws.snapshotd.type.BackupProfile;
import com.peterphi.aws.snapshotd.type.DurableVolume;

public interface BackupProfileService
{
	/**
	 * Assign a BackupProfile to the provided DurableVolume<br />
	 * Unless an exception is thrown, DurableVolume MUST have a BackupProfile returned once this method returns
	 * 
	 * @param volume
	 */
	public void assignProfile(DurableVolume volume);
}
