## Vert.x Solr Service
Vert.x 3 solr service with event bus proxying.

[![Build Status](https://travis-ci.org/ef-labs/vertx-solr-service.png)](https://travis-ci.org/ef-labs/vertx-solr-service)

TODO: Update the documents for vert.x 3 service proxying.

##Configuration

The configuration options are as follows:

```
{
    "hk2_binder" : <hk2_binder>,
    "client_type": <client_type>,
    "server_url": <server_url>
}
```

* `hk2_binder` - The fully qualified path name of your hk2_binder. 
* `client_type` - The type of vert.x Solr client, default is DefaultVertxSolrClient
* `server_url` - The url to your Solr installation.

An an example configuration would be:

```
{
    "hk2_binder": "com.englishtown.vertx.solr.hk2.SolrBinder",
    "server_url": "http://localhost:8983/solr"
}
```

The SolrVerticle requires a SolrQuerySerializer to be injected. A DefaultSolrQuerySerializer has been provided which handles serialization/deserialization of json. 

The default binding provided is for HK2, but you can create a guice module if that is your container of choice. 

### Dependency Injection and the HK2VerticleFactory

There are two ways to enable DI:

1. In the vert.x.langs.properties set the java value to: java=com.englishtown~vert-mod-hk2~1.7.0:com.englishtown.vertx.hk2.HK2VerticleFactor
2. Pass a system property at startup like this: -Dvertx.langs.java=com.englishtown~vertx-mod-hk2~1.7.0:com.englishtown.vertx.hk2.HK2VerticleFactory

See the [englishtown/vertx-mod-hk2](https://github.com/englishtown/vertx-mod-hk2) project for more details.

## Action Commands

### Query

Standard Solr query parameters are supported. In the below example, the "q", "start" and "rows" parameters are used. See the [Solr CommonQueryParameters Wiki](http://wiki.apache.org/solr/CommonQueryParameters) for query construction details. 

The service replies with a json result with the following structure:

```
{
    "responseHeader": {
        "status": 0,
        "QTime": 1
    }
    "response": {
        "numFound": <number_found>,
        "start": <start>,
        "docs": [<docs>]
    }
}
```
* `number_found` - The total number of results found.
* `start` - The starting point of this query. This number will increment based on the rows parameter passed in.
* `docs` - The docs array, which holds the query results.

An example result message would be:

```
{
    "responseHeader": {
        "status": 0,
        "QTime": 1
    }
    "response": {
        "numFound": 21,
        "start": 0,
        "docs": [
            {
                "id": "GB18030TEST",
                "name": "Test with some GB18030 encoded characters",
                "features": [
                    "No accents here",
                    "这是一个功能",
                    "This is a feature (translated)",
                    "这份文件是很有光泽",
                    "This document is very shiny (translated)"
                ],
                "price": 0,
                "price_c": "0,USD",
                "inStock": true,
                "_version_": 1475520188742893600
            },
            {
                "id": "SP2514N",
                "name": "Samsung SpinPoint P120 SP2514N - hard drive - 250 GB - ATA-133",
                "manu": "Samsung Electronics Co. Ltd.",
                "manu_id_s": "samsung",
                "cat": [
                    "electronics",
                    "hard drive"
                ],
                "features": [
                    "7200RPM, 8MB cache, IDE Ultra ATA-133",
                    "NoiseGuard, SilentSeek technology, Fluid Dynamic Bearing (FDB) motor"
                ],
                "price": 92,
                "price_c": "92,USD",
                "popularity": 6,
                "inStock": true,
                "manufacturedate_dt": 1139844397000,
                "store": "35.0752,-97.032",
                "_version_": 1475520188790079500
            },
            {
                "id": "6H500F0",
                "name": "Maxtor DiamondMax 11 - hard drive - 500 GB - SATA-300",
                "manu": "Maxtor Corp.",
                "manu_id_s": "maxtor",
                "cat": [
                    "electronics",
                    "hard drive"
                ],
                "features": [
                    "SATA 3.0Gb/s, NCQ",
                    "8.5ms seek",
                    "16MB cache"
                ],
                "price": 350,
                "price_c": "350,USD",
                "popularity": 6,
                "inStock": true,
                "store": "45.17614,-93.87341",
                "manufacturedate_dt": 1139844397000,
                "_version_": 1475520188795322400
            },
            {
                "id": "F8V7067-APL-KIT",
                "name": "Belkin Mobile Power Cord for iPod w/ Dock",
                "manu": "Belkin",
                "manu_id_s": "belkin",
                "cat": [
                    "electronics",
                    "connector"
                ],
                "features": [
                    "car power adapter, white"
                ],
                "weight": 4,
                "price": 19.95,
                "price_c": "19.95,USD",
                "popularity": 1,
                "inStock": false,
                "store": "45.18014,-93.87741",
                "manufacturedate_dt": 1122913825000,
                "_version_": 1475520188807905300
            },
            {
                "id": "IW-02",
                "name": "iPod & iPod Mini USB 2.0 Cable",
                "manu": "Belkin",
                "manu_id_s": "belkin",
                "cat": [
                    "electronics",
                    "connector"
                ],
                "features": [
                    "car power adapter for iPod, white"
                ],
                "weight": 2,
                "price": 11.5,
                "price_c": "11.50,USD",
                "popularity": 1,
                "inStock": false,
                "store": "37.7752,-122.4232",
                "manufacturedate_dt": 1139961359000,
                "_version_": 1475520188810002400
            }
        ],
    }
}
```
