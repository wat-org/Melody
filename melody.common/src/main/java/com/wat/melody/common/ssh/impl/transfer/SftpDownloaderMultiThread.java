package com.wat.melody.common.ssh.impl.transfer;

import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.LocalFileSystem;
import com.wat.melody.common.ssh.impl.SshSession;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferNoThread;
import com.wat.melody.common.transfer.Transferable;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SftpDownloaderMultiThread extends SftpBaseTransferMultiThread {

	public SftpDownloaderMultiThread(SshSession session,
			List<ResourcesSpecification> rss, int maxPar, TemplatingHandler th) {
		super(session, rss, maxPar, th);
	}

	@Override
	public String getThreadName() {
		return "downloader";
	}

	@Override
	public String getSourceSystemDescription() {
		return getSession().getConnectionDatas().toString();
	}

	@Override
	public String getDestinationSystemDescription() {
		return "local";
	}

	@Override
	public FileSystem newSourceFileSystem() {
		ChannelSftp channel = getSession().openSftpChannel();
		return new SftpFileSystem(channel);
	}

	@Override
	public FileSystem newDestinationFileSystem() {
		return new LocalFileSystem();
	}

	@Override
	public TransferNoThread newTransferNoThread(FileSystem sourceFileSystem,
			FileSystem destinationFileSystem, Transferable t) {
		return new DownloaderNoThread((SftpFileSystem) sourceFileSystem,
				destinationFileSystem, t, getTemplatingHandler());
	}

}