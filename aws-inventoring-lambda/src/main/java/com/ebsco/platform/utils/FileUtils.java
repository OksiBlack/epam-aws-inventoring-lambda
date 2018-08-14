package com.ebsco.platform.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileUtils {
public static Path findFirstDeeperInDirByName(Path start, String name) throws IOException {
	return Files.find(start, Integer.MAX_VALUE,
			(p, a) -> p.normalize()
					.endsWith(name))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Path ended with %s not found.", name)));
}

/**
 *
 * @param start
 * @param name
 * @return
 * @throws IOException
 */
public static Path findFirstDeeperInDirByTail(Path start, String name) throws IOException {
	return Files.find(start, Integer.MAX_VALUE,
			(p, a) -> p.normalize()
					.toString()
					.endsWith(name))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Path ended with %s not found.", name)));
}

/**
 *
 * @param start
 * @param name
 * @return
 * @throws IOException
 */
public static Stream<Path> findAllDeeperInDirByName(Path start, String name) throws IOException {
	return Files.find(start, Integer.MAX_VALUE,
			(p, a) -> p.normalize()
					.endsWith(name));
}

/**
 *
 * @param start
 * @param name
 * @return
 * @throws IOException
 */
public static Stream<Path> findAllDeeperInDirByTail(Path start, String name) throws IOException {
	Stream<Path> stream = Files.find(start, Integer.MAX_VALUE,
			(p, a) -> p.normalize()
					.toString()
					.endsWith(name));
	return stream;
}

/**
 * @param path
 * @return
 * @throws IOException
 */
public static ByteBuffer bytesFromFileToByteBuffer(Path path) throws IOException {

	return ByteBuffer.wrap(Files.readAllBytes(path));
}
}