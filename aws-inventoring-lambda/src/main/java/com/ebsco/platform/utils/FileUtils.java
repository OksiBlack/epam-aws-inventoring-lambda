package com.ebsco.platform.utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
public static Path findFirstDeeperInDirByName(Path start, String name) throws IOException {
	return Files.find(start, Integer.MAX_VALUE,
			(p, a) -> p.toString()
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
			(p, a) -> p
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
			(p, a) -> p.toString()
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
			(p, a) -> p
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

/**
 *
 * @param predicate
 * @return
 * @throws IOException
 */
public static List<File> findFilesInCurrentDirectoryAndSubDirectories(BiPredicate<Path, BasicFileAttributes> predicate) throws IOException {
	return Files.find(Paths.get("."), Integer.MAX_VALUE,predicate )
			.map(p -> p.toFile())
			.collect(Collectors.toList());
}

/**
 *
 * @return
 * @throws IOException
 */
public static List<File> listFilesInCurrentDirectory() throws IOException {
	return Files.list(Paths.get("."))
				.map(p -> p.toFile())
				.collect(Collectors.toList());
}
}