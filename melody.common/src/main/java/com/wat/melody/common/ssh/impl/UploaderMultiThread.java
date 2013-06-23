package com.wat.melody.common.ssh.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.MelodyInterruptedException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.types.LocalResource;
import com.wat.melody.common.ssh.types.Resources;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class UploaderMultiThread {

	private static Log log = LogFactory.getLog(UploaderMultiThread.class);

	protected static final short NEW = 16;
	protected static final short RUNNING = 8;
	protected static final short SUCCEED = 0;
	protected static final short FAILED = 1;
	protected static final short INTERRUPTED = 2;
	protected static final short CRITICAL = 4;

	private SshSession _session;
	private List<Resources> _resourcesList;
	private int _maxPar;
	private TemplatingHandler _templatingHandler;
	private List<LocalResource> _localResourcesList;

	private short _state;
	private ThreadGroup _threadGroup;
	private List<UploaderThread> _threadsList;
	private ConsolidatedException _exceptionsSet;

	protected UploaderMultiThread(SshSession session, List<Resources> r,
			int maxPar, TemplatingHandler th) {
		setSession(session);
		setResourcesList(r);
		setMaxPar(maxPar);
		setTemplatingHandler(th);
		setLocalResourcesList(new ArrayList<LocalResource>());

		markState(SUCCEED);
		setThreadGroup(null);
		setThreadsList(new ArrayList<UploaderThread>());
		setExceptionsSet(new ConsolidatedException());
	}

	protected void upload() throws UploaderException, InterruptedException {
		// compute resources to upload
		if (getResourcesList().size() == 0) {
			return;
		}
		computeLocalResources();
		if (getLocalResourcesList().size() == 0) {
			return;
		}
		// do upload
		try {
			log.debug(Msg.bind(Messages.UploadMsg_START, getSession()
					.getConnectionDatas()));
			setThreadGroup(new ThreadGroup(Thread.currentThread().getName()
					+ ">uploader"));
			getThreadGroup().setDaemon(true);
			initializeUploadThreads();
			try {
				startUploadThreads();
			} catch (InterruptedException Ex) {
				markState(INTERRUPTED);
			} catch (Throwable Ex) {
				getExceptionsSet().addCause(Ex);
				markState(CRITICAL);
			} finally {
				// If an error occurred while starting thread, some thread may
				// have been launched without any problem
				// We must wait for these threads to die
				waitForUploadThreadsToBeDone();
				quit();
				log.debug(Messages.UploadMsg_FINISH);
			}
		} finally {
			// This allow the doProcessing method to be called multiple time
			// (will certainly be useful someday)
			setThreadGroup(null);
		}
	}

	private void computeLocalResources() throws UploaderException {
		for (Resources resources : getResourcesList()) {
			try { // Add all found LocalResource to the global list
				List<LocalResource> ar = resources.findResources();
				getLocalResourcesList().removeAll(ar); // remove duplicated
				getLocalResourcesList().addAll(ar);
			} catch (IOException Ex) {
				throw new UploaderException(
						Messages.UploadEx_IO_ERROR_WHILE_FINDING, Ex);
			}
		}
		for (LocalResource r : getLocalResourcesList()) {
			System.out.println(r);
		}
	}

	protected void upload(ChannelSftp channel, LocalResource r) {
		try {
			new UploaderNoThread(channel, r, getTemplatingHandler()).upload();
		} catch (UploaderException Ex) {
			UploaderException e = new UploaderException(Msg.bind(
					Messages.UploadEx_FAILED, r), Ex);
			markState(UploaderMultiThread.FAILED);
			getExceptionsSet().addCause(e);
		}
	}

	private void initializeUploadThreads() {
		int max = getMaxPar();
		if (getLocalResourcesList().size() < max) {
			max = getLocalResourcesList().size();
		}
		for (int i = 0; i < max; i++) {
			getThreadsList().add(new UploaderThread(this, i + 1));
		}
	}

	private void startUploadThreads() throws InterruptedException {
		int threadToLaunchID = getThreadsList().size();
		List<UploaderThread> runningThreads = new ArrayList<UploaderThread>();

		while (threadToLaunchID > 0 || runningThreads.size() > 0) {
			// Start ready threads
			while (threadToLaunchID > 0 && runningThreads.size() < getMaxPar()) {
				UploaderThread ft = getThreadsList().get(--threadToLaunchID);
				runningThreads.add(ft);
				ft.startProcessing();
			}
			// Sleep a little
			if (runningThreads.size() > 0)
				runningThreads.get(0).waitTillProcessingIsDone(50);
			// Remove ended threads
			for (int i = runningThreads.size() - 1; i >= 0; i--) {
				UploaderThread ft = runningThreads.get(i);
				if (ft.getFinalState() != NEW && ft.getFinalState() != RUNNING)
					runningThreads.remove(ft);
			}
		}
	}

	private void waitForUploadThreadsToBeDone() {
		int nbTry = 2;
		while (nbTry-- > 0) {
			try {
				for (UploaderThread ft : getThreadsList())
					ft.waitTillProcessingIsDone();
				return;
			} catch (InterruptedException Ex) {
				// If the processing was stopped wait for each thread to end
				if (!isInterrupted()) {
					log.info(Messages.UploadMsg_GRACEFUL_SHUTDOWN);
				}
				markState(INTERRUPTED);
				getExceptionsSet().addCause(Ex);
			}
		}
		throw new RuntimeException("Fatal error occurred while waiting "
				+ "for " + UploaderMultiThread.class.getCanonicalName()
				+ " to finish.", getExceptionsSet());
	}

	private void quit() throws UploaderException, InterruptedException {
		for (UploaderThread ft : getThreadsList()) {
			markState(ft.getFinalState());
			if (ft.getFinalState() == FAILED || ft.getFinalState() == CRITICAL) {
				getExceptionsSet().addCause(ft.getFinalError());
			}
		}

		if (isCritical()) {
			throw new UploaderException(Msg.bind(Messages.UploadEx_UNMANAGED,
					getSession().getConnectionDatas()), getExceptionsSet());
		} else if (isFailed()) {
			throw new UploaderException(Msg.bind(Messages.UploadEx_MANAGED,
					getSession().getConnectionDatas()), getExceptionsSet());
		} else if (isInterrupted()) {
			throw new MelodyInterruptedException(Messages.UploadEx_INTERRUPTED,
					getExceptionsSet());
		}
	}

	protected short markState(short state) {
		return _state |= state;
	}

	private boolean isFailed() {
		return FAILED == (_state & FAILED);
	}

	private boolean isInterrupted() {
		return INTERRUPTED == (_state & INTERRUPTED);
	}

	private boolean isCritical() {
		return CRITICAL == (_state & CRITICAL);
	}

	protected SshSession getSession() {
		return _session;
	}

	private SshSession setSession(SshSession session) {
		if (session == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + SshSession.class.getCanonicalName()
					+ ".");
		}
		SshSession previous = getSession();
		_session = session;
		return previous;
	}

	protected List<Resources> getResourcesList() {
		return _resourcesList;
	}

	private List<Resources> setResourcesList(List<Resources> resources) {
		if (resources == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ Resources.class.getCanonicalName() + ">.");
		}
		List<Resources> previous = getResourcesList();
		_resourcesList = resources;
		return previous;
	}

	protected int getMaxPar() {
		return _maxPar;
	}

	/**
	 * @param maxPar
	 *            is the maximum number of {@link UploaderThread} this object
	 *            can run simultaneously.
	 * 
	 * @throws ForeachException
	 *             if the given value is not >= 1 and < 10.
	 */
	private int setMaxPar(int maxPar) {
		if (maxPar < 1) {
			maxPar = 1; // security
		} else if (maxPar > 10) {
			maxPar = 10; // maximum number of opened JSch channel
		}
		int previous = getMaxPar();
		_maxPar = maxPar;
		return previous;
	}

	private TemplatingHandler getTemplatingHandler() {
		return _templatingHandler;
	}

	private TemplatingHandler setTemplatingHandler(TemplatingHandler th) {
		TemplatingHandler previous = getTemplatingHandler();
		_templatingHandler = th;
		return previous;
	}

	/**
	 * @return the list of {@link LocalResource}, computed from this object's
	 *         {@link Resources}.
	 */
	protected List<LocalResource> getLocalResourcesList() {
		return _localResourcesList;
	}

	private List<LocalResource> setLocalResourcesList(List<LocalResource> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ LocalResource.class.getCanonicalName() + ">.");
		}
		List<LocalResource> previous = getLocalResourcesList();
		_localResourcesList = aft;
		return previous;
	}

	/**
	 * @return the {@link ThreadGroup} which holds all {@link ForeachThread}
	 *         managed by this object.
	 */
	protected ThreadGroup getThreadGroup() {
		return _threadGroup;
	}

	private ThreadGroup setThreadGroup(ThreadGroup tg) {
		// Can be null
		ThreadGroup previous = getThreadGroup();
		_threadGroup = tg;
		return previous;
	}

	/**
	 * @return all the {@link UploaderThread} managed by this object.
	 */
	private List<UploaderThread> getThreadsList() {
		return _threadsList;
	}

	private List<UploaderThread> setThreadsList(List<UploaderThread> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ UploaderThread.class.getCanonicalName() + ">.");
		}
		List<UploaderThread> previous = getThreadsList();
		_threadsList = aft;
		return previous;
	}

	/**
	 * @return the exceptions that append during the upload.
	 */
	private ConsolidatedException getExceptionsSet() {
		return _exceptionsSet;
	}

	private ConsolidatedException setExceptionsSet(ConsolidatedException cex) {
		if (cex == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ConsolidatedException.class.getCanonicalName() + ".");
		}
		ConsolidatedException previous = getExceptionsSet();
		_exceptionsSet = cex;
		return previous;
	}

}