package com.ebsco.platform.aqa.utils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Executable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MiscTestHelperUtils {

/**
 * @param filePrefix
 * @param fileSuffix
 * @param repeatNum
 * @return
 * @throws IOException
 */
public static File createSampleFile(String filePrefix,
									String fileSuffix, int repeatNum) throws IOException {
	Path file = Files.createTempFile(filePrefix, fileSuffix);

	try (Writer writer = Files.newBufferedWriter(file)) {
		for (int i = 0; i < repeatNum; i++) {
			StringBuilder line = IntStream.iterate(i * 100, j -> j + 2)
					.limit(200)
					.collect(StringBuilder::new, StringBuilder::append,
							StringBuilder::append);

			StringBuilder odd = IntStream.iterate((i + 1) * 100, j -> j + 2)
					.limit(200)
					.collect(StringBuilder::new, StringBuilder::append,
							StringBuilder::append);

			String chars = IntStream.range(0, 127)
					.mapToObj(ch -> String.valueOf((char) ch))
					.collect(Collectors.joining(" ",
							"[", "]"));
			writer.write(line.insert(0, "[")
					.append("]")
					.toString());
			writer.write(odd.insert(0, "[")
					.append("]")
					.toString());
			writer.write(chars);

		}
	}
	return file.toRealPath().toFile();
}

}
