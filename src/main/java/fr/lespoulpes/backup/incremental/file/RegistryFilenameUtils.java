package fr.lespoulpes.backup.incremental.file;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import fr.lespoulpes.backup.incremental.registry.IRegistry;

public class RegistryFilenameUtils {
	public static String extension = "registry";
	private static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmssSSS";

	public static String toFilename(IRegistry<?, ?> registry) {
		return toFilenamePrefix(registry) + "." + extension;
	}
	
	public static String toFilenamePrefix(IRegistry<?, ?> registry) {
		return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date(registry.getTimestamp()));
	}

	public static Comparator<String> filenameComparator(boolean reverse) {
		return (a, b) -> {
			SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
			try {
				Date ad = sdf.parse(StringUtils.substringBefore(a, "." + extension));
				Date bd = sdf.parse(StringUtils.substringBefore(b, "." + extension));
				return reverse ? ad.compareTo(bd) * -1 : ad.compareTo(bd);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		};
	}
}
