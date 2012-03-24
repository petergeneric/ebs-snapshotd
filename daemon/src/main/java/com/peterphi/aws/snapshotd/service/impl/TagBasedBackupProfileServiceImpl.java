package com.peterphi.aws.snapshotd.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.ec2.model.Tag;
import com.peterphi.aws.snapshotd.service.iface.BackupProfileService;
import com.peterphi.aws.snapshotd.service.iface.NotificationService;
import com.peterphi.aws.snapshotd.type.BackupProfile;
import com.peterphi.aws.snapshotd.type.DurableVolume;
import com.peterphi.aws.snapshotd.type.StandardAWSTag;
import com.peterphi.aws.snapshotd.util.AWSTagHelper;

public class TagBasedBackupProfileServiceImpl implements BackupProfileService
{
	private static final transient Logger log = Logger.getLogger(TagBasedBackupProfileServiceImpl.class);

	protected final Map<String, BackupProfile> profiles = new HashMap<String, BackupProfile>();
	protected AWSTagHelper tagHelper = new AWSTagHelper();
	protected NotificationService notifier;

	public void addProfile(BackupProfile profile)
	{
		if (profiles.containsKey(profile.getName()))
			throw new IllegalArgumentException("Profile by name " + profile.getName() + " already exists!");

		profiles.put(profile.getName(), profile);
	}

	public AWSTagHelper getTagHelper()
	{
		return tagHelper;
	}

	public void setTagHelper(AWSTagHelper tagHelper)
	{
		this.tagHelper = tagHelper;
	}

	public NotificationService getNotifier()
	{
		return notifier;
	}

	public void setNotifier(NotificationService notifier)
	{
		this.notifier = notifier;
	}

	@Override
	public void assignProfile(DurableVolume volume)
	{
		final Set<String> profileNames = getAllProfileNames(volume);

		if (profileNames.size() == 1)
		{
			// Get the single profile name
			final String profileName = profileNames.iterator().next();

			if (profileName != null)
			{
				final BackupProfile profile = getProfile(profileName);

				if (profile == null)
					throw new IllegalArgumentException("DurableVolume uses unrecognised Backup Profile name: " + profileName);
				else
					volume.setBackupProfile(profile);
			}
			else
			{
				throw new IllegalArgumentException("DurableVolume has no backup profile: will not process");
			}
		}
		else if (profileNames.size() == 0)
		{
			throw new IllegalArgumentException("DurableVolume has no Volume and no Snapshots!");
		}
		else
		{
			throw new IllegalArgumentException("DurableVolume contents have inconsistent profile name tags: " + profileNames);
		}
	}

	protected Set<String> getAllProfileNames(DurableVolume volume)
	{
		final Set<String> profileNames = new HashSet<String>();

		// If there is a Volume, extract the profile from its Tags
		if (volume.getVolume() != null)
		{
			final List<Tag> tags = volume.getVolume().getTags();

			profileNames.add(getProfileName(tags));
		}

		// If there are Snapshots, extract the profile from their Tags
		if (!volume.getSnapshots().isEmpty())
		{
			for (Snapshot snapshot : volume.getSnapshots())
			{
				final String profileName = getProfileName(snapshot.getTags());

				profileNames.add(profileName);
			}
		}

		return profileNames;
	}

	/**
	 * Determine the backup profile (if any) to assign a
	 * 
	 * @param tags
	 * @return
	 */
	protected BackupProfile getProfile(String name)
	{
		return profiles.get(name);
	}

	protected String getProfileName(List<Tag> tags)
	{
		return tagHelper.read(tags, StandardAWSTag.BACKUP_PROFILE);
	}
}
