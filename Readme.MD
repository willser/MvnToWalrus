## Intro

[Mvn to walrus](https://github.com/willser/MvnToWalrus)  is a mvn plugin to upload file to walrus in mvn lifecycle.

## Require

`jdk 17` and `mvn 3.9`

## Try

```mvn package```

You will get log like this,it means the jar of module `test` has been uploaded to walrus.:

```
[INFO] --- walrus:1.0-SNAPSHOT:upload (default) @ test ---
[INFO] Upload file `<your workspakce path>/target/test-1.0-SNAPSHOT.jar` to Walrus
[INFO] Status: Already certified
[INFO] BlobId: 8vLrldsU8E2yKV2Nh-e__eOa4h9doFZi0-PAwYRNlUg
[INFO] EndEpoch: 1
[INFO] SuiRefType: Previous Sui Certified Event
[INFO] SuiRef: Ftyxk7TBTkxPpzqYyEVtsvWFjsZc7vag7z7Qs83eSrhM
[INFO] SuiBaseUrl: https://suiscan.xyz/testnet/tx/Ftyxk7TBTkxPpzqYyEVtsvWFjsZc7vag7z7Qs83eSrhM
[INFO] Download url: https://aggregator-devnet.walrus.space/v1/8vLrldsU8E2yKV2Nh-e__eOa4h9doFZi0-PAwYRNlUg
```

Download jar from walrus
```shell
curl -o test.jar https://aggregator-devnet.walrus.space/v1/8vLrldsU8E2yKV2Nh-e__eOa4h9doFZi0-PAwYRNlUg
```

Run the jar
```shell
java -jar test.jar
```

You will get log like this:
```
Hello Walrus
```

It works!