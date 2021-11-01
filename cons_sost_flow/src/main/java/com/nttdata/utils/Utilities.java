package com.nttdata.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.FileSystemUtils;

public class Utilities {
	
	/**
	 * 
	 * @param srcDir
	 * @param destDir
	 * @return
	 */
	public boolean copyFilesFromOneDirToAnother(String srcDir,String destDir) {
		File src = new File(srcDir);
		File dest = new File(destDir);
		try {
			FileSystemUtils.copyRecursively(src, dest);
			System.out.println("Copia dei files dalla dir. " + srcDir + " alla dir. " + destDir + " eseguito con successo");
			return true;
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("Si è verificato un errore durante la copia dei files dalla dir. " + srcDir + " alla dir. " + destDir + " Dettaglio: " + e.getMessage());
			return false;
		}
	}

	/**
	 * 
	 * @param srcFile
	 * @return
	 */
	public static String checkFileExist(String srcFile) {
		File tempFile = new File(srcFile);
		boolean exists = false;
		try {
			exists = tempFile.exists();
		} catch (Exception e) {
			exists = false;
			System.out.println("Si è verificato un errore in checkFileExist. Dettaglio: " + e.getMessage());
		}
		return exists == true ? "SI" : "NO";
	}

	/**
	 * 
	 * @param sourcePath
	 * @param targetPath
	 * @return
	 */
	public boolean moveFile(String sourcePath, String targetPath) {
		File fileToMove = new File(sourcePath);
		return fileToMove.renameTo(new File(targetPath));
	}

	/**
	 * 
	 * @param srcDir
	 * @return
	 */
	public boolean creaDirectory(String srcDir) {
		boolean directoryCreata = false;
		try {
			Path path = Paths.get(srcDir);
			Files.createDirectories(path);
			System.out.println("La Directory " + srcDir + " è stata creata!");
			directoryCreata = true;
		} catch (IOException e) {
			System.err.println("Si è verificato un errore durante la creazione della directory " + srcDir
					+ " nella working dir! Dettaglio: " + e.getMessage());
		}
		return directoryCreata;
	}

	/**
	 * 
	 * @return
	 */
	public String generateDirname() {

		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");
		Date now = new Date();
		String strDate = sdfDate.format(now);

		UUID uuid = UUID.randomUUID();
		String dirname = strDate + "_" + uuid.toString();
		return dirname;

	}

	/**
	 * 
	 * @param srcPath
	 */
	public boolean deleteAllContentOfDirectory(String srcPath) {
		System.out.println(srcPath + " vale");
		return deleteDir(new File(srcPath));
	}

	/**
	 * 
	 * @param dir
	 */
	public boolean deleteDir(File dir) {
		boolean contenutoCancellato = false;
		try {
			FileUtils.cleanDirectory(dir);
			contenutoCancellato = true;
		} catch (IOException e) {
			System.out.println("");
			contenutoCancellato = false;
		}
		return contenutoCancellato;

	}
	
	/**
	 * 
	 * @param source
	 * @param file
	 * @param tos
	 * @param fNames
	 * @throws IOException
	 */
	private void addFilesToTarGZ(String source, File file, TarArchiveOutputStream tos, File[] fNames)
			throws IOException {

		if (file.isDirectory())
			for (File cFile : file.listFiles()) {
				addFilesToTarGZ(cFile.getAbsolutePath(), cFile, tos, fNames);
			}
		else {
			tos.putArchiveEntry(new TarArchiveEntry(file, source));
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			try {
				IOUtils.copy(bis, tos);
				tos.closeArchiveEntry();
			} finally {
				fis.close();
				bis.close();
			}
		}
	}
	
	/**
	 * 
	 * @param source
	 * @param pathZippedFilename
	 */
	public void createTarFile(String source, String pathZippedFilename) {
		TarArchiveOutputStream tarOs = null;
		try {

			FileOutputStream fos = new FileOutputStream(pathZippedFilename);
			GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
			tarOs = new TarArchiveOutputStream(gos);
			tarOs.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
			File folder = new File(source);
			File[] fileNames = folder.listFiles();

			for (File file : fileNames) {
				addFilesToTarGZ(file.getName(), file, tarOs, fileNames);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				tarOs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * 
	 * @param sourceDir
	 */
	public void createTarFile(String sourceDir) {
		TarArchiveOutputStream tarOs = null;
		try {
			File source = new File(sourceDir);
			// Using input name to create output name
			FileOutputStream fos = new FileOutputStream(source.getAbsolutePath().concat(".tar.gz"));
			GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
			tarOs = new TarArchiveOutputStream(gos);
			addFilesToTarGZ(sourceDir, "", tarOs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				tarOs.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param filePath
	 * @param parent
	 * @param tarArchive
	 * @throws IOException
	 */
	private void addFilesToTarGZ(String filePath, String parent, TarArchiveOutputStream tarArchive) throws IOException {
		File file = new File(filePath);
		// Create entry name relative to parent file path
		String entryName = parent + file.getName();
		// add tar ArchiveEntry
		tarArchive.putArchiveEntry(new TarArchiveEntry(file, entryName));
		if (file.isFile()) {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			// Write file content to archive
			IOUtils.copy(bis, tarArchive);
			tarArchive.closeArchiveEntry();
			bis.close();
		} else if (file.isDirectory()) {
			// no need to copy any content since it is
			// a directory, just close the outputstream
			tarArchive.closeArchiveEntry();
			// for files in the directories
			for (File f : file.listFiles()) {
				// recursively call the method for all the subdirectories
				addFilesToTarGZ(f.getAbsolutePath(), entryName + File.separator, tarArchive);
			}
		}
	}

}
