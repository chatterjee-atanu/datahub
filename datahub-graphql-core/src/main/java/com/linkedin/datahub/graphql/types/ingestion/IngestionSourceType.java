package com.linkedin.datahub.graphql.types.ingestion;

import com.google.common.collect.ImmutableSet;
import com.linkedin.common.urn.Urn;
import com.linkedin.common.urn.UrnUtils;
import com.linkedin.datahub.graphql.QueryContext;
import com.linkedin.datahub.graphql.generated.IngestionSource;
import com.linkedin.entity.EntityResponse;
import com.linkedin.entity.client.EntityClient;
import graphql.execution.DataFetcherResult;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.linkedin.metadata.Constants;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class IngestionSourceType implements com.linkedin.datahub.graphql.types.LoadableType<IngestionSource, String>{
    public static final Set<String> ASPECTS_TO_FETCH =
            ImmutableSet.of(Constants.INGESTION_SOURCE_ENTITY_NAME, Constants.INGESTION_INFO_ASPECT_NAME, Constants.INGESTION_SOURCE_KEY_ASPECT_NAME);

    @Override
    public Class<IngestionSource> objectClass() {
        return IngestionSource.class;
    }

    @Override
    public List<DataFetcherResult<IngestionSource>> batchLoad(
            @Nonnull List<String> urns, @Nonnull QueryContext context) throws Exception {
        final List<Urn> ingestionSourceUrns =
                urns.stream().map(UrnUtils::getUrn).collect(Collectors.toList());

        try {
            final Map<Urn, EntityResponse> entities =
                    _entityClient.batchGetV2(
                            context.getOperationContext(),
                            Constants.INGESTION_SOURCE_ENTITY_NAME,
                            new HashSet<>(ingestionSourceUrns),
                            ASPECTS_TO_FETCH);

            final List<EntityResponse> gmsResults = new ArrayList<>();
            for (Urn urn : ingestionSourceUrns) {
                gmsResults.add(entities.getOrDefault(urn, null));
            }
            return gmsResults.stream()
                    .map(
                            gmsResult ->
                                    gmsResult == null
                                            ? null
                                            : DataFetcherResult.<IngestionSource>newResult()
                                            .data(IngestionSourceMapper.map(context, gmsResult))
                                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to batch load Ingestion sources", e);
        }
    }

    private final EntityClient _entityClient;
}
