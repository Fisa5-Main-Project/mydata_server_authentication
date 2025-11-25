package com.knowwhohow.global.db;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        // 현재 트랜잭션이 'Read Only'인지 확인
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

        // Read Only면 SLAVE, 아니면(쓰기) MASTER 반환
        return isReadOnly ? DataSourceType.SLAVE : DataSourceType.MASTER;
    }
}
