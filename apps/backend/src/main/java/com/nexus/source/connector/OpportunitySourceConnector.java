package com.nexus.source.connector;

public interface OpportunitySourceConnector {
    String getSourceKey();
    SourceMetadata getMetadata();
    SyncResult fetchOpportunities(SyncRequest request);
}
