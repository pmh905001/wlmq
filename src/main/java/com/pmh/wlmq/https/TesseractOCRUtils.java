/**
 * 
 */
package com.pmh.wlmq.https;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class TesseractOCRUtils {
	private static final Logger logger = Logger
			.getLogger(TesseractOCRUtils.class.getName());
	private static final String TESSERACT_OCR_CMD = "tesseract";

	/**
	 * OCR the image specifed by image path into text string.
	 * 
	 * @param imagePath
	 *            path of the image. Can be absolute path or relative path which
	 *            is relative to this application working directory.
	 * @return the text string presented in the image.
	 */
	public static String ocrImage2Text(String imagePath) {
		logger.log(Level.INFO, "Begin OCR image file path: {0}", imagePath);
		// Default empty return string
		String text = "";

		File imageFile = new File(imagePath);
		File workingDirFile = imageFile.getParentFile();
		String resultTextFileName = "result" + System.currentTimeMillis();
		// The output text file will be named with ".txt" extension by OCR.
		String resultTextFileFullPath = workingDirFile.getAbsolutePath()
				+ File.separator + resultTextFileName + ".txt";

		// tesseract-ocr command usage:
		// tesseract imageFileName outputTxtFileName
		ArrayList<String> command = new ArrayList<String>();
		command.add(TESSERACT_OCR_CMD);
		command.add(imageFile.getName());
		command.add(resultTextFileName);
		logger.log(Level.INFO, "OCR command line: {0}", command);

		Process ocrProcess = null;
		StringBuilder result = new StringBuilder();
		BufferedReader resultTextFileReader = null;
		String msg = "OK.";

		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.directory(workingDirFile);
			logger.log(Level.INFO, "OCR command working dir: {0}",
					workingDirFile.getAbsolutePath());
			ocrProcess = pb.start();
			int ret = ocrProcess.waitFor();
			logger.log(Level.INFO, "OCR process return: {0}", ret);
			switch (ret) {
			case 0:
				resultTextFileReader = new BufferedReader(
						new InputStreamReader(new FileInputStream(
								resultTextFileFullPath), "UTF-8"));
				String str;
				while ((str = resultTextFileReader.readLine()) != null) {
					result.append(str);
				}
				text = result.toString();
				break;
			case 1:
				msg = "Errors accessing files.There may be spaces in your image's filename.";
				break;
			case 29:
				msg = "Cannot recongnize the image or its selected region.";
				break;
			case 31:
				msg = "Unsupported image format.";
				break;
			default:
				msg = "Errors occurred.";
				break;
			}
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Caught exception!", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Caught exception!", e);
			e.printStackTrace();
		} finally {
			if (resultTextFileReader != null) {
				try {
					resultTextFileReader.close();
				} catch (IOException e) {
					// ignore
					logger.log(Level.WARNING, "close FileInputStream failed!",
							e);
				}
			}
		}

		// Delete temple output txt result file
		boolean deleteOK = new File(resultTextFileFullPath).delete();
		logger.log(Level.INFO, "Delete temple output txt result file: {0} {1}",
				new Object[] { resultTextFileFullPath, deleteOK });
		logger.log(Level.INFO, "OCR message: {0}", msg);

		logger.log(Level.INFO, "End OCR image file path: {0}", imagePath);
		return text;
	}
}
