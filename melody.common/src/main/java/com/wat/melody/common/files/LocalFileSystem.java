package com.wat.melody.common.files;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.NotLinkException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;

import org.apache.commons.io.FileUtils;

import com.wat.melody.common.files.exception.WrapperAccessDeniedException;
import com.wat.melody.common.files.exception.WrapperDirectoryNotEmptyException;
import com.wat.melody.common.files.exception.WrapperFileAlreadyExistsException;
import com.wat.melody.common.files.exception.WrapperNoSuchFileException;
import com.wat.melody.common.files.exception.WrapperNotDirectoryException;
import com.wat.melody.common.files.exception.WrapperNotLinkException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocalFileSystem implements FileSystem {

	public LocalFileSystem() {
	}

	@Override
	public void release() {
	}

	@Override
	public boolean exists(Path path, LinkOption... options) {
		return Files.exists(path, options);
	}

	@Override
	public boolean isDirectory(Path path, LinkOption... options) {
		return Files.isDirectory(path, options);
	}

	@Override
	public boolean isRegularFile(Path path, LinkOption... options) {
		return Files.isRegularFile(path, options);
	}

	@Override
	public boolean isSymbolicLink(Path path) {
		return Files.isSymbolicLink(path);
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs)
			throws IOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException {
		try {
			Files.createDirectory(dir, attrs);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (FileAlreadyExistsException Ex) {
			if (!isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
				throw new WrapperFileAlreadyExistsException(Ex.getFile());
			}
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
	}

	@Override
	public void createDirectories(Path dir, FileAttribute<?>... attrs)
			throws IOException, FileAlreadyExistsException,
			AccessDeniedException {
		try {
			Files.createDirectories(dir, attrs);
		} catch (FileAlreadyExistsException Ex) {
			throw new WrapperFileAlreadyExistsException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
	}

	@Override
	public void createSymbolicLink(Path link, Path target,
			FileAttribute<?>... attrs) throws IOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException {
		try {
			Files.createSymbolicLink(link, target, attrs);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (FileAlreadyExistsException Ex) {
			throw new WrapperFileAlreadyExistsException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
	}

	@Override
	public Path readSymbolicLink(Path link) throws IOException,
			NoSuchFileException, NotLinkException, AccessDeniedException {
		try {
			// /!\ This will remove the trailing '/'
			return Paths.get(Files.readSymbolicLink(link).toString());
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (NotLinkException Ex) {
			throw new WrapperNotLinkException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
	}

	@Override
	public void delete(Path path) throws IOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		try {
			Files.delete(path);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (DirectoryNotEmptyException Ex) {
			throw new WrapperDirectoryNotEmptyException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
	}

	@Override
	public boolean deleteIfExists(Path path) throws IOException,
			DirectoryNotEmptyException, AccessDeniedException {
		try {
			return Files.deleteIfExists(path);
		} catch (DirectoryNotEmptyException Ex) {
			throw new WrapperDirectoryNotEmptyException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
	}

	@Override
	public void deleteDirectory(Path dir) throws IOException {
		/*
		 * TODO : exception thrown by this method doesn't respect the contract
		 */
		FileUtils.deleteDirectory(dir.toFile());
	}

	@Override
	public EnhancedFileAttributes readAttributes(Path path) throws IOException,
			AccessDeniedException {
		try {
			return _readAttributes(path);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
	}

	private EnhancedFileAttributes _readAttributes(Path path)
			throws IOException, NoSuchFileException, AccessDeniedException {
		BasicFileAttributes pathAttrs = readAttributes0(path,
				LinkOption.NOFOLLOW_LINKS);
		Path target = null;
		BasicFileAttributes realAttrs = null;
		if (pathAttrs.isSymbolicLink()) {
			target = readSymbolicLink(path);
			try {
				realAttrs = readAttributes0(path);
			} catch (NoSuchFileException ignored) {
			}
		}
		return new LocalFileAttributes(pathAttrs, target, realAttrs);
	}

	private BasicFileAttributes readAttributes0(Path path,
			LinkOption... options) throws IOException, NoSuchFileException,
			AccessDeniedException {
		return Files.readAttributes(path, BasicFileAttributes.class, options);
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path path)
			throws IOException, NotDirectoryException, NoSuchFileException {
		try {
			return Files.newDirectoryStream(path);
		} catch (NoSuchFileException Ex) {
			throw new WrapperNoSuchFileException(Ex.getFile());
		} catch (NotDirectoryException Ex) {
			throw new WrapperNotDirectoryException(Ex.getFile());
		} catch (AccessDeniedException Ex) {
			throw new WrapperAccessDeniedException(Ex.getFile());
		}
	}

}