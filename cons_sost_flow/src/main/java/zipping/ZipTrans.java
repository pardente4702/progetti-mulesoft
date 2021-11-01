package zipping;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;

import com.google.common.io.Files;

public class ZipTrans {

	final static Logger logger = Logger.getLogger(ZipTrans.class);
	
	/*
	public static void main(String[] args) {

		try {
			prepareConsSostZipFile("D:/cancellare/", "D:/pippo.zip", "D:/pippo2.zip", "D:/prova", "pippo");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/

	public String prepareConsSostZipFile(String srcFolder, String fileZip, String backFileZip, String path,
			String relativePath) throws Exception {
		zipFolder(srcFolder, fileZip, backFileZip, path, relativePath);
		return "OK";
	}

	public String compress(String SOURCE_FOLDER, String pathZippedFilename, String backPathZippedFilename) {

		createTarFile(SOURCE_FOLDER, pathZippedFilename);

		// crea la copia di backup copiando il file generato e non
		// rigenerando di nuovo il file
		try {
			Files.copy(new File(pathZippedFilename), new File(backPathZippedFilename));
		} catch (IOException ioe) {
			logger.warn("Errore durante la creazione della copia di backup del file: " + pathZippedFilename);
			return null;
		}
		// createTarFile(SOURCE_FOLDER, backPathZippedFilename);

		return "Success";
	}

	static public void zipFolder(String srcFolder, String destZipFile, String destZipFileBack, String path,
			String relativePath) throws Exception {

		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;

		// ZipOutputStream zipBack = null;
		// FileOutputStream fileWriterBack = null;

		fileWriter = new FileOutputStream(destZipFile);
		zip = new ZipOutputStream(fileWriter);

		// fileWriterBack = new FileOutputStream(destZipFileBack);
		// zipBack = new ZipOutputStream(fileWriterBack);

		addFolderToZip(path, srcFolder, zip, relativePath);

		// addFolderToZip(path, srcFolder, zipBack, relativePath);

		zip.flush();
		zip.close();

		// zipBack.flush();
		// zipBack.close();

		// crea la copia di backup copiando il file generato e non
		// rigenerando di nuovo il file
		Files.copy(new File(destZipFile), new File(destZipFileBack));

	}

	static private void addFileToZip(String path, String srcFile, ZipOutputStream zip, String relativePath,
			String fileType) throws Exception {

		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip, relativePath);
		} else {
			byte[] buf = new byte[1024];
			int len;

			@SuppressWarnings("resource")
			FileInputStream in = new FileInputStream(srcFile);

			try {
				if (fileType != "xml")
					zip.putNextEntry(new ZipEntry(relativePath + "/" + folder.getName()));
				else
					zip.putNextEntry(new ZipEntry(folder.getName()));

				while ((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
			} finally {
				// to avoid locking the input file
				in.close();
			}
		}
	}

	static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip, String relativePath)
			throws Exception {
		File folder = new File(srcFolder);

		for (String fileName : folder.list()) {
			String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
			if (!(ext.equals("xml")))
				addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip, relativePath, "pdf");
			else
				addFileToZip(path, srcFolder + "/" + fileName, zip, relativePath, "xml");
		}
	}

	private void createTarFile(String source, String pathZippedFilename) {
		TarArchiveOutputStream tarOs = null;
		try {

			FileOutputStream fos = new FileOutputStream(pathZippedFilename);
			GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
			tarOs = new TarArchiveOutputStream(gos);
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
}