package com.nhl.link.move.runtime.extractor.model;

import com.nhl.link.move.LmRuntimeException;
import com.nhl.link.move.extractor.model.ExtractorModelContainer;
import com.nhl.link.move.runtime.LmRuntimeBuilder;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * An {@link IExtractorModelLoader} that resolves
 * {@link ExtractorModelContainer} locations against a specified root directory.
 * 
 * @since 1.4
 */
public class FileExtractorModelLoader extends BaseExtractorModelLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileExtractorModelLoader.class);

	private File rootDir;

	public FileExtractorModelLoader(@Inject(LmRuntimeBuilder.FILE_EXTRACTOR_MODEL_ROOT_DIR) File rootDir) {
		LOGGER.info("Extractor XML files will be located under '{}'", rootDir);
		this.rootDir = rootDir;
	}

	@Override
	protected Reader getXmlSource(String name) throws IOException {

		File file = getFile(name);

		LOGGER.info("Will extract XML from {}", file.getAbsolutePath());

		return new InputStreamReader(new FileInputStream(file), "UTF-8");
	}

	@Override
	public boolean needsReload(ExtractorModelContainer container) {
		return container.getLoadedOn() < getFile(container.getLocation()).lastModified();
	}

	protected File getFile(String name) {
		if (!name.endsWith(".xml")) {
			name += ".xml";
		}

		File file = new File(rootDir, name);

		if (!file.isFile()) {
			throw new LmRuntimeException(file.getAbsolutePath() + " is not a file");
		}

		return file;
	}

}
