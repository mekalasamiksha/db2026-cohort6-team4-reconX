package com.dbtraining.reconx.domain;

import com.dbtraining.reconx.model.TradeType.AssetClass;
import jakarta.persistence.*;

@Entity(name = "LegacyInstrument")
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_class", nullable = false, length = 20)
    private AssetClass assetClass;

    @Column(nullable = false, length = 3)
    private String currency;

    /**
        * Legacy metadata blob for the audited domain entity.
     */
        @Lob
        @Column(name = "metadata", columnDefinition = "CLOB")
        private String metadata;

    // --- getters / setters omitted for brevity ---
}