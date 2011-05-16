package com.jamespot.glifpix.util;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.zip.CRC32;

import com.jamespot.glifpix.resources.ResourcesHandler;

public class Utils {
	
	public static String getCRC(String str) {
		
		return Long.toString(getCRCAsLong(str));
	}
	public static long getCRCAsLong(String str) {
		CRC32 crc = new CRC32();
		crc.update(str.getBytes(Charset.forName("UTF-16")));
		return crc.getValue();
	}
	
	public static String getMd5(String str) throws NoSuchAlgorithmException {
		byte[] hash = MessageDigest.getInstance("MD5").digest(str.getBytes());
		StringBuilder hashString = new StringBuilder();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(hash[i]);
			if (hex.length() == 1) {
				hashString.append('0');
				hashString.append(hex.charAt(hex.length() - 1));
			} else
				hashString.append(hex.substring(hex.length() - 2));
		}
		return hashString.toString();
	}

	public static Properties cmdLineParse(String className, String[] args)
	{
		Properties cmdLineprops = new Properties();

		String currentKey = "";
		for (String arg : args) {
			if (arg.startsWith("--")) {
				currentKey = "";
				cmdLineprops.put(arg.replaceFirst("--", ""), "");
			} else if (arg.startsWith("-")) {
				currentKey = arg.replaceFirst("-", "");
			} else if (!currentKey.equals("")) {
				cmdLineprops.put(currentKey, arg);
			}
		}
		
		
		if (cmdLineprops.size() == 0 || cmdLineprops.containsKey("help") || !(cmdLineprops.containsKey("propfile"))) {
			usage(className, null);
		}

		Properties props = new Properties();
		try {
			props.load(new FileInputStream(cmdLineprops.getProperty("propfile")));
		} catch (Exception e) {
			usage(className, "Unable to read : " + cmdLineprops.getProperty("propfile") + " " + e.getMessage());
		}

		props.putAll(cmdLineprops);
		
		// Check glifpix.home
		if (!(props.containsKey("glifpix.home")) || props.getProperty("glifpix.home").equals("SetThisValue")) {
			usage(className, "glifpix.home is not set");
		}
		return props;

	}
	
	public static void usage(String className, String errMsg) {
		System.out.println("java " + className + " [--help] -propfile <configurationFile> [...] \n");
		System.out.println("");
		System.out.println(" Reads <configurationFile> to start " + className);
		System.out.println(" Arguments [...] from cmdLine will override configurationFile values");
		if (errMsg != null) {
			System.out.println(" Error : " + errMsg + "\n");
		}
		System.exit(-1);
	}


}
