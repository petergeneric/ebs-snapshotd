package com.github.petergeneric.aws.elasticsnap.daemon.service;

import com.github.petergeneric.aws.elasticsnap.daemon.type.BackupProfile;
import com.github.petergeneric.aws.elasticsnap.daemon.type.DurableVolume;

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
