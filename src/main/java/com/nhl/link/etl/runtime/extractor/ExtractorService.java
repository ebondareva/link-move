package com.nhl.link.etl.runtime.extractor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cayenne.di.Inject;

import com.nhl.link.etl.extractor.Extractor;
import com.nhl.link.etl.extractor.model.ExtractorName;
import com.nhl.link.etl.runtime.EtlRuntimeBuilder;
import com.nhl.link.etl.runtime.extractor.model.IExtractorModelService;

public class ExtractorService implements IExtractorService {

	private IExtractorModelService modelService;
	private ConcurrentMap<ExtractorName, Extractor> extractors;
	private Map<String, IExtractorFactory> factories;

	public ExtractorService(@Inject IExtractorModelService modelService,
			@Inject(EtlRuntimeBuilder.EXTRACTOR_FACTORIES_MAP) Map<String, IExtractorFactory> factories) {
		this.factories = factories;
		this.modelService = modelService;
		this.extractors = new ConcurrentHashMap<>();
	}

	@Override
	public Extractor getExtractor(ExtractorName name) {

		Extractor extractor = extractors.get(name);

		if (extractor == null) {
			Extractor newExtractor = new ReloadableExtractor(modelService, factories, name);
			Extractor oldExtractor = extractors.putIfAbsent(name, newExtractor);
			extractor = oldExtractor != null ? oldExtractor : newExtractor;
		}

		return extractor;
	}

}