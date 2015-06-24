package au.id.villar.crypt.cl;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {

	private static final int SALT_LENGTH = 16;

	public static void main(String[] args) {

		validateArgs(args);

		Path file = Paths.get(args[0]);
		char[] password = askPassword();

		if(!Files.exists(file)) save(Collections.emptyList(), file, password);

		String action;
		while(!(action = askInput("enter action ('h' for menu)")).toLowerCase().equals("q")) {
			switch(action) {
				case "a": addRecord(file, password); break;
				case "h": printMenu(); break;
				case "m": moveRecord(file, password); break;
				case "p": printRecords(file, password); break;
				case "r": removeRecord(file, password); break;
			}
		}
		System.out.println("DONE");
	}

	private static String askInput(String prompt) {
		System.out.print(prompt + "> ");
		return new Scanner(System.in).nextLine();
	}

	private static void printMenu() {
		System.out.format(
				"%nMENU%n----------------------%n" +
						"a\tAdd record%n" +
						"h\tMenu%n" +
						"m\tMove record%n" +
						"p\tPrint all records%n" +
						"q\tQuit%n" +
						"r\tRemove record%n%n"
		);
	}

	private static void addRecord(Path file, char[] password) {

		String description = askInput("Type entry (or empty to cancel)");

		if(description.isEmpty()) return;

		List<String> list = load(file, password);
		list.add(description);
		save(list, file, password);

	}

	private static void removeRecord(Path file, char[] password) {

		String lineNumber = askInput("Type line number (or empty to cancel)");

		if(lineNumber.isEmpty()) return;

		int number;
		try { number = Integer.valueOf(lineNumber); } catch(NumberFormatException e ) { return; }
		if(number < 0) return;
		number--;

		List<String> list = load(file, password);
		if(number >= list.size()) return;
		list.remove(number);
		save(list, file, password);

	}

	private static void moveRecord(Path file, char[] password) {

		int source = askNumber("Type source line number (or empty to cancel)");
		if(source == -1) return;

		int destination = askNumber("Type destination line number (or empty to cancel)");
		if(destination == -1) return;

		if(destination > source) destination--;

		List<String> list = load(file, password);
		if(source >= list.size() || destination >= list.size()) return;
		list.add(destination, list.remove(source));
		save(list, file, password);

	}

	private static int askNumber(String prompt) {
		String lineNumber = askInput(prompt);
		if(lineNumber.isEmpty()) return -1;
		try { return Integer.valueOf(lineNumber) - 1; } catch(NumberFormatException e ) { return -1; }
	}

	private static void printRecords(Path file, char[] password) {

		List<String> list = load(file, password);

		System.out.println();
		for(int index = 0; index < list.size(); index++) {
			System.out.format("#%02d: %s%n", index + 1, list.get(index));
		}
		System.out.println();

	}

	private static void validateArgs(String ... args) {
		if(args.length != 1) {
			System.out.format("%nUSAGE:%njava %s _file_%n%nwhere _file_ is the file with a list of passwords%n%n",
					Main.class.getCanonicalName());

			System.exit(-1);
		}
	}

	private static void exitWithError(String description) {
		System.out.format("ERROR: %s%n%nFINISHED%n%n", description);
		System.exit(-1);
	}

	private static List<String> load(Path file, char[] password) {

		Charset charset = Charset.forName("UTF-8");
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		EncryptUtil encryptUtil = new EncryptUtil(SALT_LENGTH);
		try(InputStream input = Files.newInputStream(file)) {
			encryptUtil.decrypt(input, buffer, password);
		} catch (IOException | GeneralSecurityException e) {
			exitWithError("loading file: " + e.getMessage());
		}

		int start = 0;
		int end;
		byte[] arrayBuffer = buffer.toByteArray();
		List<String> list = new ArrayList<>();
		while((end = getNextIndexOfZero(start, arrayBuffer)) != -1) {
			list.add(new String(arrayBuffer, start, end - start, charset));
			start = end + 1;
		}
		return list;
	}

	private static int getNextIndexOfZero(int fromIndex, byte[] buffer) {
		for(int index = fromIndex; index < buffer.length; index++) {
			if(buffer[index] == 0) return index;
		}
		return -1;
	}

	private static void save(List<String> list, Path file, char[] password) {

		Charset charset = Charset.forName("UTF-8");
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		try {
			for (String item : list) {
				buffer.write(item.getBytes(charset));
				buffer.write(0);
			}
		} catch (IOException e) {
			exitWithError("saving file: " + e.getMessage());
		}

		EncryptUtil encryptUtil = new EncryptUtil(SALT_LENGTH);
		try(OutputStream output = Files.newOutputStream(file)) {
			InputStream input = new ByteArrayInputStream(buffer.toByteArray());
			encryptUtil.encrypt(input, output, password);
		} catch (IOException | GeneralSecurityException e) {
			exitWithError("saving file: " + e.getMessage());
		}

	}

	private static char[] askPassword() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Password: ");
		char[] password = scanner.nextLine().toCharArray();
		char[] normalized = new char[32];
		for(int i = 0; i < normalized.length; i++) {
			normalized[i] = password[i % password.length];
		}
		return normalized;
	}

}
